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
