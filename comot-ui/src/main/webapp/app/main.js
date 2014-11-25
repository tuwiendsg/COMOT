requirejs.config({
	paths : {
		text : '../lib/require/text',
		durandal : '../lib/durandal/js',
		plugins : '../lib/durandal/js/plugins',
		transitions : '../lib/durandal/js/transitions',
		knockout : '../lib/knockout/knockout-3.1.0',
		komapping : '../lib/knockout/knockout.mapping',
		jquery : '../lib/jquery/jquery-1.9.1',
		crel : '../lib/json-human/crel',
		json_human : '../lib/json-human/json.human',
		d3 : '../lib/d3/d3.min',
		comot_client : '../js/comot-client',
		pnotify : '../lib/pnotify/pnotify.custom.min',
		shim : {
			komapping : {
				deps : [ 'knockout' ],
				exports : 'komapping'
			},
			comot_client : {
				deps : [ 'jquery' ],
				exports : 'comot_client'
			}
		}
	}
});

define(function(require) {
	var system = require('durandal/system'), app = require('durandal/app');

	system.debug(true);

	app.title = 'CoMoT';

	app.configurePlugins({
		router : true,
		dialog : true
	});

	app.start().then(function() {
		app.setRoot('shell');
	});
});