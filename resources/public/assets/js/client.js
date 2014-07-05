
function getSlider(id) { return sliders[id]; }

function setSelected(div, paper) {
  selectedPaper = paper;
  selectedDiv = div;
  $('.spermdiv').attr('class','spermdiv');
  div.addClass('spermdiv-selected');
  updateManual(myospermglyph.server._getDefsForPaper(selectedDiv.attr('id')));
};

function selectAnimalPreset(str) {
  var dict = myospermglyph.server._drawAnimalPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
  updateManual(dict);
};

function selectHumanPreset(str) {
  var dict = myospermglyph.server._drawHumanPreset(selectedDiv, str, [selectedDiv.width(), selectedDiv.height()]);
  updateManual(dict);
};

function updateManual(dict) {
  for(i in dict) {
	if(sliders[i] != null)
	  sliders[i].setValue(dict[i]);
  }
}

function manualProps() {
  var obj = {};
  for(key in sliders) {
	var sl = sliders[key];
	obj[key] = parseFloat(sl.getValue());
  }
  return obj;
};

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

var pcGridDivs = {};
var prevSels = [];
function updatePCGrid(selected) {
  var newentries = {};
  var removed = _.difference(prevSels,_.pluck(selected,'id'));
  prevSels = _.pluck(selected,'id');

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
	//var paper = pcGridDivs[id].paper;
	var div = pcGridDivs[id].div; 
	div.show();
	//if(paper == null) {
	  var paper = myospermglyph.server._drawParams(div, params, [div.width(), div.height()]);
	  //pcGridDivs[id].svg = div.children().first();
	//}
	// else {
	 // console.log('Appending paper',paper,'to',div);
	  //div.append(paper);
	// }
  });
}


$(document).ready(function() {
  myospermglyph.server._init('/assets/data/'); 
  var cellsize = [175,175];
  var margins = [10,10];
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

  $('.create-slider').each(function(i) {
	var sl = $(this).slider().on('slide', function(ev) {
	  constrainSliders();
	  myospermglyph.server._drawManual(selectedDiv, [selectedDiv.width(), selectedDiv.height()], manualProps());
	}).data('slider');
	sliders[$(this).prop('id')] = sl;
  });

  papers = [];
  papers = _.union(papers, [myospermglyph.server._draw($('#spermbig'), [(cellsize[0]+margins[0])*2,(cellsize[1]+margins[1])*2])]);
  setSelected($('#spermbig'),papers[0]);
  
  for(i in [0,1,2,3,4])
	papers = _.union(papers, [myospermglyph.server._draw($('#spermsmall'+i.toString()), cellsize)]);

  $('.human-preset-link').attr('href',function(d) { return 'javascript:selectHumanPreset(\"' + $(this).attr('id') + '\");'; });
  $('.animal-preset-link').attr('href',function(d) { return 'javascript:selectAnimalPreset(\"' + $(this).attr('id') + '\");'; });

  var i = 0;
  _.each(papers, function(p) {
	 $(p.canvas).css({'pointer-events': 'none'});
	 $(p.canvas).parent().click(function(div) {  setSelected($(div.currentTarget),p); });
	 i++;
  });

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
