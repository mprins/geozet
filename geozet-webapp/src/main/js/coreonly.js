var geozetCORE = {

	init : function() {
		this.resetHTML();
	},

	resetHTML : function() {
		var url = '' + window.location;
		// url = url.replace(/coreonly/g, 'corefalse');

		document.getElementById('geozetEnhanced').className = 'nothidden';
		document.getElementById('geozetEnhanced').innerHTML = '<a href="' + url
				+ '">Ga naar de interactieve kaartversie.</a>';
	}

};

geozetCORE.init();
