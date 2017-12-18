function showSimpleProteinViewer(pdbFilePath, viewerId) {

	var options = {
		width : 300,
		height : 300,
		antialias : true,
		quality : 'medium'
	};

	// check if a viewer was already initialized
	if (document.getElementById(viewerId).childNodes.length == 0) {

		// insert the viewer under the Dom element with id 'gl'.
		var viewer = pv.Viewer(document.getElementById(viewerId), options);
		$.ajax(pdbFilePath).done(function(data) {
			// parse
			var structure = io.pdb(data);
			mol.assignHelixSheet(structure);
			// create instance rendered to specified DOM element
			// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
			// defaultPVOptions);
			viewer.ballsAndSticks('structure', structure);
			// viewer.lines('structure', structure);
			viewer.centerOn(structure);
			// move camera to a reasonable position
			viewer.autoZoom();
		});
	}
}

function showAlignmentProteinViewer(pdbFilePath1, pdbFilePath2, viewerId) {

	var options = {
		width : 300,
		height : 300,
		antialias : true,
		quality : 'medium'
	};

	// insert the viewer under the Dom element with id 'gl'.
	var viewer = pv.Viewer(document.getElementById(viewerId), options);
	$.ajax(pdbFilePath1).done(function(data) {
		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);
		viewer.ballsAndSticks('structure', structure);
		// viewer.lines('structure', structure);
		viewer.centerOn(structure);
		// move camera to a reasonable position
		viewer.autoZoom();
	});
	$.ajax(pdbFilePath2).done(function(data) {
		// parse
		var structure = io.pdb(data);
		mol.assignHelixSheet(structure);
		// create instance rendered to specified DOM element
		// protein.viewer = pv.Viewer($('#V' + protein.id)[0],
		// defaultPVOptions);
		viewer.ballsAndSticks('structure', structure);
		// viewer.lines('structure', structure);
		viewer.centerOn(structure);
		// move camera to a reasonable position
		viewer.autoZoom();
	});
}
