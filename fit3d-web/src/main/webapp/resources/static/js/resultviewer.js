var pv = pv.Viewer(document.getElementById('viewer'), {

	width : 300,
	height : 300,
	antialias : true,
	quality : 'medium'
});

function showPairwiseAlignment(pdbFilePath1, pdbFilePath2) {

	// clear structures
	pv.clear();

	// insert the viewer under the Dom element with id 'gl'.
	$.ajax(pdbFilePath1).done(function(data) {
		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);
		pv.ballsAndSticks('structure', structure);
		// viewer.lines('structure', structure);
		pv.centerOn(structure);
		// move camera to a reasonable position
		pv.autoZoom();

	});
	$.ajax(pdbFilePath2).done(function(data) {

		var color2 = [ 0, 1, 0, 0.7 ];

		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);

		// color reference structure in red
		pv.ballsAndSticks('structure', structure, {
			color : color.uniform(color2)
		});
		// viewer.lines('structure', structure);
		pv.centerOn(structure);
		// move camera to a reasonable position
		pv.autoZoom();
	});
}

function load(pdb_id) {
	document.getElementById('status').innerHTML = 'loading ' + pdb_id;
	var xhr = new XMLHttpRequest();
	xhr.open('GET', 'pdbs/' + pdb_id + '.pdb');
	xhr.setRequestHeader('Content-type', 'application/x-pdb');
	xhr.onreadystatechange = function() {
		if (xhr.readyState == 4) {
			structure = io.pdb(xhr.responseText);
			preset();
			pv.centerOn(structure);
		}
		document.getElementById('status').innerHTML = '';
	}
	xhr.send();
}

function showAllAgainstOneAlignment(pdbFilePath1, pdbFilePath2) {

	// clear structures
	pv.clear();

	// insert the viewer under the Dom element with id 'gl'.
	$.ajax(pdbFilePath1).done(function(data) {
		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);
		pv.lines('structure', structure);
		pv.centerOn(structure);
		// move camera to a reasonable position
		pv.autoZoom();

	});
	$.ajax(pdbFilePath2).done(function(data) {

		var color2 = [ 0, 1, 0, 0.7 ];

		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);

		// color reference structure in red
		pv.ballsAndSticks('structure', structure, {
			color : color.uniform(color2)
		});
		// viewer.lines('structure', structure);
		pv.centerOn(structure);
		// move camera to a reasonable position
		pv.autoZoom();
	});
}