define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {
		// properties
		services : ko.observableArray(),
		// functions
		newService : newService,
		viewService : viewService,
		updateService : updateService,
		removeService : removeService,
		newInstanceService : newInstanceService,
		viewInstance : viewInstance,
		removeInstance : removeInstance,
		// life-cycle
		detached : function() {

		},
		deactivate : function() {

		},
		activate : function() {
			model.services.removeAll();

			comot.getServices(function(services) {

				for (var i = 0; i < services.length; i++) {
					var service = services[i];
					model.services.push({
						'name' : service.name,
						'id' : service.id,
						'dateCreated' : service.dateCreated,
						'instances' : ko.observableArray(service.ServiceInstances.Instance)
					});
				}
			})
		},
		attached : function() {

		}
	}

	return model;

	// SERVICE
	function newService() {

	}

	function viewService(serviceId) {

	}

	function updateService(serviceId) {

	}

	function removeService(serviceId) {

	}

	function newInstanceService(serviceId) {

		comot.createServiceInstance(serviceId, function(data) {
			var instanceId = data;

			comot.getServiceInstance(serviceId, instanceId, function(data) {

				var newInstance = data.service.ServiceInstances.Instance[0];

				for (var i = 0; i < model.services().length; i++) {
					if (model.services()[i].id === serviceId) {
						model.services()[i].instances.push(newInstance);
						break;
					}
				}

			})
		})

	}

	// INSTANCE
	function viewInstance(serviceId, instanceId) {
		console.log("serviceId: " + serviceId + ", instance:" + instanceId);
		console.log("view");
	}

	function removeInstance(serviceId, instanceId) {

	}

});
