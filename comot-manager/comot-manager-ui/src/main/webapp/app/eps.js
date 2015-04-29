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
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {
		// properties
		services : ko.observableArray(),
		// functions
		newEpsInstance : newEpsInstance,
		removeEpsInstance : removeEpsInstance,
		// life-cycle
		activate : function() {
			refreshEps();
		},
	}

	return model;

	function refreshEps() {
		model.services.removeAll();

		comot.getEpsDynamic(function(epses) {
			comot.getEpsInstancesDynamic(function(epsesInstances) {

				var viewEpses = [];

				for (var i = 0; i < epses.length; i++) {

					var eps = epses[i];
					var map = {}
					var epsInstances = [];

					viewEpses[i] = {
						'id' : eps.id,
						'templateId' : eps.serviceTemplate.id,
						'instances' : ko.observableArray()
					};

					for (var j = 0; j < epsesInstances.length; j++) {
						var oneEpsIn = epsesInstances[j];

						if (oneEpsIn.osu.id === eps.id) {
							epsInstances.push({
								'id' : oneEpsIn.id,
								'serviceId' : oneEpsIn.service.id,
								'dateCreatedFormated' : utils.longToDateString(oneEpsIn.service.dateCreated),
							});
						}
					}

					viewEpses[i].instances(epsInstances);
				}

				model.services(viewEpses);
			});
		});

	}

	function newEpsInstance(eps) {

		comot.createDynamicEps(eps.id, function(data) {
			refreshEps();
		})

	}

	function removeEpsInstance(epsId, epsInstanceId) {

		console.log(epsId + " " + epsInstanceId);

		comot.removeDynamicEps(epsId, epsInstanceId, function(data) {

			for (var i = 0; i < model.services().length; i++) {
				if (model.services()[i].id === epsId) {

					model.services()[i].instances.remove(function(item) {
						return item.id === epsInstanceId;
					});
				}
			}
		}, "Could not remove the dynamic EPS");

	}

});
