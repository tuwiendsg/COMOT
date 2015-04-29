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

	var notify = require('notify');

	var model = {
		// properties
		serviceId : ko.observable(""),
		actionPlans : ko.observableArray(),
		// functions

		// life-cycle
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {

			comot.getElasticActions(model.serviceId(), function(data) {

				// var viewData = $.extend(true, {}, data);

				for (var i = 0; i < data.length; i++) {
					var one = data[i];
					one.timestamp = utils.longToDateString(one.timestamp);

					delete data[i].changeTimestamp;

					for (var j = 0; j < one.actions.length; j++) {
						one.actions[j].timestamp = utils.longToDateString(one.actions[j].timestamp);
					}

					var human = JsonHuman.format(one);
					var tmp = document.createElement("div");
					tmp.appendChild(human);

					one.html = tmp.innerHTML;
				}

				model.actionPlans(data)
			});
		},
		detached : function() {

		}
	};

	return model;

});
