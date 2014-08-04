/*
 * SpermZoo, by Simon Walton
 * An implementation of Glyph-Based Video Visualization for Semen Analysis
 */

var pc_data;

/*
 * create the parallel coordinates infrastructure
 */
function createSpermPC(pc_data, placement, gridplacement, size, colorGroup, ignore) {
	var parcoords = d3.parcoords()(placement)
		.data(pc_data)
		.allowNonQuantitative()
		.alpha(0.55)
		.colorGroup(colorGroup)
		.height(size[1])
		.width(size[0])
		.colorMap(getColorMap())
		.alpha(1.0)
		.shadows()
		.vertical()
		.margin({
			top: 10,
			left: 20,
			right: 20,
			bottom: 10
		})
		.colorMap(d3.scale.ordinal().range(["#F15A29","#068587"]))
		.ignore(ignore)
		.reorderable()
		.brushable()
		.render();

	// setting up grid
	var column_keys = _.reject(d3.keys(pc_data[0]), function(d) { return _.contains(ignore, d); });
	
	// update grid on brush
	parcoords.on("brush", function (d) {
		updatePCGrid(d);
	});
	parcoords.on("brushend", function(d) {});

	return parcoords;
}

var category10 = [ "#A8DCD9","#28AAC5",  "#6B6B6B" , "#FBB040", "#F7941E", "#69D2E7", "#F15A29"];
function getColorMap() {
	return d3.scale.ordinal().range(category10);
}



