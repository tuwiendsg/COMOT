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
define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), dimple = require('dimple'), JsonHuman = require('json_human'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var repeaterModule = require('repeater');
	var moduleTime = require('analysis/times');
	var moduleActions = require('analysis/actions');
	var notify = require('notify');

	var model = {
		// properties
		serviceId : ko.observable(""),
		tabs : ko.observableArray([ {
			name : 'Elastic actions',
			module : 'analysis/actions',
			show : ko.observable(true),
		}, {
			name : 'Deployment times',
			module : 'analysis/times',
			show : ko.observable(false),
		} ]),
		// functions
		showTab : showTab,
		// life-cycle
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {
		},
		detached : function() {
		}
	};

	function showTab(toBeActivated) {

		for (var i = 0; i < model.tabs().length; i++) {
			var tab = model.tabs()[i];

			if (tab.name === toBeActivated.name) {
				tab.show(true);
			} else {
				tab.show(false);
			}
		}
	}

	return model;

});
