/***********************************************************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 * 
 * This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8
 * \#317790)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');
	var tosca = {
		value : null
	};
	var toscaTemplate = {
		value : null
	};

	var model = {
		// properties
		templates : ko.observableArray(),
		services : ko.observableArray(),
		// functions
		newService : newService,
		newTemplate : newTemplate,
		removeService : removeService,
		newServiceFromTemplate : newServiceFromTemplate,
		removeTemplate : removeTemplate,
		// life-cycle
		activate : function() {
			refreshServices();
			refreshTemplates();
		},
		attached : function() {
			fileToString("fileTosca", tosca);
			fileToString("fileToscaTemplate", toscaTemplate);
		}
	}

	return model;

	function newServiceFromTemplate(template) {

		comot.createServiceFromTemplate(template.id, function(data) {
			
			refreshServices();
			notify.success("Service '" + data + "' created.");
			
		}, "Failed to create a new service from the template '"+template.id+"'.")
	}

	function newService(form) {

		if (tosca.value == null) {
			notify.error("No xml file selected.");
			return;
		}

		comot.createServiceTosca(tosca.value, function(data) {
			
			notify.success("Service '" + data + "' created.");
			refreshServices();
			
		}, "Failed to create the service.");
	}

	function newTemplate(form) {

		if (toscaTemplate.value == null) {
			notify.error("No xml file selected.");
			return;
		}

		comot.createTemplateTosca(toscaTemplate.value, function(data) {
			
			notify.success("Template " + data + " created.");
			refreshTemplates();
			
		}, "Failed to create the template.");
	}

	function removeService(service) {

		comot.removeService(service.id, function(){
			
			notify.success("Service " + service.id + " removed.");
			model.services.remove(service);
			
		}, "Failed to remove the service '"+service.id+"'.");

	}

	function removeTemplate(template) {

		comot.removeTemplate(template.id, function() {

			notify.success("Template " + template.id + " removed.");
			model.templates.remove(template);

		}, "Failed to remove template " + template.id);

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

	function fileToString(elementId, resultVar) {

		var filesInput = document.getElementById(elementId);

		filesInput.addEventListener("change", function(event) {

			var file = event.target.files[0];

			if (!file.type.match('text/xml')) {
				notify.error(file.name + " is not an xml file!");
				resultVar.value = null;
				return;
			}

			var reader = new FileReader();

			reader.addEventListener("load", function(event) {
				resultVar.value = event.target.result;
			});
			reader.readAsText(file);
		});

	}

});
