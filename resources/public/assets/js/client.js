
/*
 * SpermZoo, by Simon Walton
 * An implementation of Glyph-Based Video Visualization for Semen Analysis
 */

/* --------------------------- *
 * general interface           *
 * --------------------------  */

/*
 * set the sperm on the main grid to be the focus of param changes 
 */
function setSelected(div, paper) {
	selectedPaper = paper;
	selectedDiv = div;
	$('.spermdiv').attr('class','spermdiv');
	div.addClass('spermdiv-selected');
	updateManual(myospermglyph.server._getDefsForPaper(selectedDiv.attr('id')));
};

/*
 * set the selected sperm to get params from an animal preset with a given preset id
 */
function selectAnimalPreset(str) {
	var dict = myospermglyph.server._drawAnimalPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
	updateManual(dict);
};

/*
 * set the selected sperm to get params from a human preset with a given preset id
 */
function selectHumanPreset(str) {
	var dict = myospermglyph.server._drawHumanPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
	updateManual(dict);
};

/*
 * set the manual sliders to match the parameters given by the dictionary object. the keys must match the
 * id's of the sliders 
 */
function updateManual(dict) {
	for(i in dict) {
		if(sliders[i] != null)
			sliders[i].setValue(dict[i]);
	}
}

/*
 * opposite of above: return a dictionary representing the values of the parameter slides.
 */
function manualProps() {
	var obj = {};
	for(key in sliders) {
		var sl = sliders[key];
		obj[key] = parseFloat(sl.getValue());
	}
	return obj;
};

/*
 * apply constraints to the sliders - called each time the user moves a slider
 * example, vap < vsl < vcl
 */
function constrainSliders() {
	var vsl = parseFloat(sliders['vsl'].getValue());
	var vap = parseFloat(sliders['vap'].getValue());
	var vcl = parseFloat(sliders['vcl'].getValue());
	if(vsl > vap) {
		sliders['vsl'].setValue(sliders['vap'].getValue());
	}
	if(vap > vcl) {
		sliders['vap'].setValue(sliders['vcl'].getValue());
	}
}

/* --------------------------- *
 * parallel coordinates        *
 * --------------------------  */

// a div for each grid item
var pcGridDivs = {};
// 
var prevSels = [];

/*
 * update the sperm visible in the grid when the user modifies the parallel coordinates brush state 
 */
function updatePCGrid(selected) {
	// figure out what's new, and removed
	var newentries = {};
	var removed = _.difference(prevSels,_.pluck(selected,'id'));
	prevSels = _.pluck(selected,'id');

	// for each one removed, hide it
	_.each(removed, function(id) {
		console.log('hiding div',pcGridDivs[id]);
		pcGridDivs[id].div.hide();
	});

	// keep pcGridDivs up to date (add only; we never remove)
	// [id] -> [str_markup, { params }]
	_.each(_.values(selected), function(item) {
		if(!_.has(pcGridDivs,item.id))
		pcGridDivs[item.id] = {div: $('#pc-result-box-'+item.id), params: item, svg: null};
	});

	// for each entry in current selection, draw it
	var i = 1;
	_.each(_.values(selected), function(item) {
		var id = item.id;
		var params = pcGridDivs[id].params;
		var div = pcGridDivs[id].div; 
		div.show();
		var paper = myospermglyph.server._drawParams(div, params, [div.width(), div.height()]);
		//pcGridDivs[id].svg = div.children().first();
		//}
		// else {
		// console.log('Appending paper',paper,'to',div);
		//div.append(paper);
		// }
		});
}

/* --------------------------- *
 *  entry point                *
 * --------------------------  */
$(document).ready(function() {
	// initialise the main cljs bits
	myospermglyph.server._init('/assets/data/'); 

	// some default sizes
	var cellsize = [175,175];
	var margins = [10,10];

	// setup gridster with a bunch of default sperms
	gridster = $('.gridster ul').gridster({
		widget_base_dimensions: cellsize,
			 widget_margins: margins, 
			 resize: {
				 enabled: true, 
			 resize: function(e, ui, $widget) {
				 var me = $widget.children().first();
				 myospermglyph.server._draw(me, [this.resize_coords.data.width, this.resize_coords.data.height]);
			 },
			 }
	}).data('gridster');

	// for each slider control, set an onchange handler
	$('.create-slider').each(function(i) {
		var sl = $(this).slider({formater: function(val) { return parseFloat(val).toFixed(2); }} ).on('slide', function(ev) {
			constrainSliders();
			myospermglyph.server._drawManual(selectedDiv, [selectedDiv.width(), selectedDiv.height()], manualProps());
		}).data('slider');
		sliders[$(this).prop('id')] = sl;
	});

	// ask the rendering parts to draw the sperm onto raphael papers and store the paper objects
	papers = [myospermglyph.server._draw($('#spermbig'), [(cellsize[0]+margins[0])*2,(cellsize[1]+margins[1])*2])];
	setSelected($('#spermbig'),papers[0]);

	// the small ones require special treatment
	for(i in [0,1,2,3,4])
		papers = _.union(papers, [myospermglyph.server._draw($('#spermsmall'+i.toString()), cellsize)]);

	// for each preset link, set its href to the appropriate selectors
	$('.human-preset-link').attr('href',function(d) { return 'javascript:selectHumanPreset(\"' + $(this).attr('id') + '\");'; });
	$('.animal-preset-link').attr('href',function(d) { return 'javascript:selectAnimalPreset(\"' + $(this).attr('id') + '\");'; });

	// for each paper object, set its onclick handler and some styling
	_.each(papers, function(p) {
		$(p.canvas).css({'pointer-events': 'none'});
		$(p.canvas).parent().click(function(div) {  setSelected($(div.currentTarget),p); });
	});

	// reset brushes in parallel coordinates click handler
	$('#reset-brushes').click(function() { parcoords.brushReset(); });

	// create the floats for the pc results
	$('a[data-toggle=\"tab\"]').on('shown.bs.tab', function (e) {
		if(e.target.hash == '#explore') {
			parcoords = createSpermPC(allData, '#explore-pc', '#explore-grid', [550,600], 'group',
					['img','name','desc','note']);
			parcoords.render();
			$('#left-grid').hide();
			$('#left-explore').show();
			parcoords.brushReset();
		}
		else {
			$('#left-grid').show();
			$('#left-explore').hide();
			//freePCGrid();
		}
	});
});
