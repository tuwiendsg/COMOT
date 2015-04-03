requirejs.config({
	paths : {
		text : '../lib/require/text',
		durandal : '../lib/durandal/js',
		plugins : '../lib/durandal/js/plugins',
		transitions : '../lib/durandal/js/transitions',
		knockout : '../lib/knockout/knockout-3.3.0',
		komapping : '../lib/knockout/knockout.mapping',
		jquery : '../lib/jquery/jquery-1.9.1',
		crel : '../lib/json-human/crel',
		json_human : '../lib/json-human/json.human',
		d3 : '../lib/d3/d3.min',
		dimple : '../lib/dimple/dimple.v2.1.2.min',
		pnotify : '../lib/pnotify/pnotify.custom.min',
		bootstrap : '../lib/bootstrap/js/bootstrap.min',

		notify : '../js/notifications',
		comot_client : '../js/comot-client',
		comot_utils : '../js/comot-utils',
		repeater : '../js/repeatedAction',

		shim : {
			'komapping' : {
				deps : [ 'knockout' ],
				exports : 'komapping'
			},
			'bootstrap' : {
				deps : [ 'jquery' ]
			}
		}
	}
});

define(function(require) {
	var system = require('durandal/system'), app = require('durandal/app');
	// require('bootstrap');

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