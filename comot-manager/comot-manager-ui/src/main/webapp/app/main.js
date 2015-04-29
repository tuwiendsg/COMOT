/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
requirejs.config({
	paths : {
		text : '../lib/require/text',
		durandal : '../lib/durandal/js',
		plugins : '../lib/durandal/js/plugins',
		transitions : '../lib/durandal/js/transitions',
		knockout : '../lib/knockout/knockout-3.3.0',
		komapping : '../lib/knockout/knockout.mapping',
		jquery : '../lib/jquery/jquery-1.11.2.min',
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