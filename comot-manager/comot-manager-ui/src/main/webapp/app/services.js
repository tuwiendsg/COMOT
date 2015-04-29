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
		templates : ko.observableArray(),
		services : ko.observableArray(),
		// functions
		newService : newService,
		viewTosca : viewTosca,
		removeService : removeService,
		newServiceFromTemplate : newServiceFromTemplate,
		removeTemplate : removeTemplate,
		// life-cycle
		activate : function() {
			refreshServices();
			refreshTemplates();
		},
	}

	return model;

	function removeTemplate(template) {

		comot.removeTemplate(template.id, function() {

			notify.success("Template " + template.id + " removed.");
			model.templates.remove(template);

		}, "Failed to remove template " + template.id);

	}

	function refreshTemplates() {

		model.templates.removeAll();

		comot.getTemplatesNonEps(function(templates) {

			for (var i = 0; i < templates.length; i++) {
				var template = templates[i];
				template.dateCreatedFormated = utils.longToDateString(template.description.dateCreated);
			}

			model.templates(templates);
		});
	}

	// SERVICE
	function newService() {

	}

	function viewTosca(object) {

	}

	function removeService(service) {

		comot.removeService(service.id, function() {

			notify.success("Service " + service.id + " removed.");
			model.services.remove(service);

		}, "Failed to remove service " + service.id);

	}

	function refreshServices() {
		model.services.removeAll();

		comot.getServicesNonEps(function(services) {

			for (var i = 0; i < services.length; i++) {
				var service = services[i];
				service.dateCreatedFormated = utils.longToDateString(service.dateCreated);
			}

			model.services(services);
		})
	}

	function newServiceFromTemplate(template) {

		comot.createServiceFromTemplate(template.id, function(data) {

			refreshServices();
		})

	}

});
