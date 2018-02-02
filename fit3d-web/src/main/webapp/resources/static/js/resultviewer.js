var defaultPVOptions = {
    fog : false,
    width : 300,
    height : 300,
    antialias : true,
    quality : 'high',
    animateTime : 0,
    selectionColor : '#f00',
    fog : false
};

var aminoAcidNames = {
    'ALA' : 'A', 'ARG' : 'R', 'ASN' : 'N', 'ASP' : 'D', 'CYS' : 'C', 'GLN' : 'Q', 'GLU' : 'E', 'GLY' : 'G', 'HIS' : 'H', 'ILE' : 'I', 'LEU' : 'L', 'LYS' : 'K', 'MET' : 'M', 'PHE' : 'F', 'PRO' : 'P', 'SER' : 'S', 'THR' : 'T', 'TRP' : 'W', 'TYR' : 'Y', 'VAL' : 'V'
}

var pvInstances = {};
var structures = {};

function viewer(opts) {
    // propagate instructions
    var id = opts.id ? opts.id : 'viewer';

    // get reference or init a new one if none exists - same for structures
    var instance;
    var structure;
    if(pvInstances[id] && !opts.clear) {
        instance = pvInstances[id];
    } else {
        $('#' + id).html('');
        instance = pv.Viewer(document.getElementById(id), defaultPVOptions);
        if(typeof(opts.motif) !== 'undefined') {
            pvInstances = {};
            structures = {};
        }
        pvInstances[id] = instance;
    }

    // pdb file needs to be fetched - show it!
    if(typeof(opts.pdb) !== 'undefined') {
        loadStructure(instance, id, opts);
    }

    // multiple structures
    if(typeof(opts.additionalPdb) !== 'undefined') {
        loadStructure(instance, id, opts.additionalPdb);
    }

    structure = structures[id];

    // get present structure and highlight selected residues during motif extraction
    if(typeof(opts.highlight) !== 'undefined') {
        var sel = structure.full().createEmptyView();
        opts.highlight.forEach(function(res) {
            var tmp = res.split("-");
            var cname = tmp[2];
            var rnum = +tmp[3];
            var add = instance.get('structure').select({
                cname : cname,
                rnum : rnum
            });
            add.atoms().forEach(function(a) {
                sel.addAtom(a);
            });
        });
        instance.get('structure').setSelection(sel);
        instance.requestRedraw();
    }
}

function loadStructure(instance, id, opts) {
    $.ajax(opts.pdb).success(function(data) {
        var structure = io.pdb(data);
        mol.assignHelixSheet(structure);
        structures[id] = structure;

        var geom;
        var style = opts.style ? opts.style : 'sticks';
        switch (style) {
            case 'cartoon':
                geom = instance.cartoon('structure', structure);
                break;
            case 'sticks':
                geom = instance.ballsAndSticks('structure', structure);
                break;
            case 'lines':
                geom = instance.lines('structure', structure);
                break;
        }

        // color if requested
        if(typeof(opts.color) !== 'undefined') {
            geom.colorBy(pv.color.uniform(opts.color));
        }

        // same for opacity
        if(typeof(opts.opacity) !== 'undefined') {
            instance.forEach(function(object) {
                object.setOpacity(opts.opacity);
            });
        }

        instance.centerOn(structure);
        instance.autoZoom();

        if(typeof(opts.motif) !== 'undefined') {
            var labelFontColor = opts.labelColor ? opts.labelColor : "rgb(0, 0, 0)";
            var labelFontSize = opts.labelSize ? opts.labelSize : "16";
            var labelAlpha = opts.labelAlpha ? opts.labelAlpha : 0;
            var labelFontStyle = opts.labelStyle ? opts.labelStyle : "normal";
            var motif = opts.motif;
            motif.forEach(function(go) {
                var tmp = go.split("-");
                var cname = tmp[0];
                var rnum = +tmp[1].substring(1);
                var residue = structure.residueSelect(function(res) {
                    return res.chain().name() === cname && res.num() === rnum;
                });

                var pos = (opts.alternatePosition && residue.atoms()[1]) ? residue.atoms()[0].pos() : residue.atoms()[1].pos();
                instance.label("label", go,
                    pos,
                    { 	"font" : "Open Sans",
                        "fontSize" : labelFontSize,
                        "backgroundAlpha" : labelAlpha,
                        "fontColor" : labelFontColor,
                        "fontStyle" : labelFontStyle
                    });
                var geom = instance.ballsAndSticks('motif', residue);
                // color if requested
                if(typeof(opts.color) !== 'undefined') {
                    geom.colorBy(pv.color.uniform(opts.color));
                }
            });

            instance.fitTo(structure.residueSelect(function(res) {
                var value = false;
                motif.forEach(function(go) {
                    var tmp = go.split("-");
                    var cname = tmp[0];
                    var rnum = +tmp[1].substring(1);
                    if (cname == res.chain().name() && rnum == res.num()) {
                        value = true;
                    }
                });
                return value;
            }));
        }


        // handle labels
        if(opts.labels) {
            labels(instance, structure, opts);
        }
    });
}

function labels(instance, structure, opts) {
    var labelFontColor = opts.labelColor ? opts.labelColor : "rgb(0, 0, 0)";
    var labelFontSize = opts.labelSize ? opts.labelSize : "16";
    var labelAlpha = opts.labelAlpha ? opts.labelAlpha : 0;
    var labelFontStyle = opts.labelStyle ? opts.labelStyle : "normal";

    for (var i = 0; i < structure.chains().length; i++) {
        var currentChain = structure.chains()[i];
        for (var j = 0; j < currentChain.residues().length; j++) {
            var currentResidue = currentChain.residues()[j];
            var pos = opts.alternatePosition ? currentResidue.atom("CA").pos() : currentResidue.center();
            instance.label("label",
                currentChain.name() + "-" + convertThreeLetterCode(currentResidue.name()) + currentResidue.num(),
                pos,
                { 	"font" : "Open Sans",
                    "fontSize" : labelFontSize,
                    "backgroundAlpha" : labelAlpha,
                    "fontColor" : labelFontColor,
                    "fontStyle" : labelFontStyle
                });
        }
    }
}

function convertThreeLetterCode(name) {
    return aminoAcidNames[name] ? aminoAcidNames[name] : 'X';
}