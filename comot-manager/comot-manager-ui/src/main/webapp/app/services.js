define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), $ = require("jquery"), router = require('plugins/router');

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
			comot.getServices(function(data) {
				model.services = komapping.fromJS(data);
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

				var newInstance = komapping.fromJS(data.service.ServiceInstances.Instance[0]);

				for (var i = 0; i < model.services().length; i++) {
					if (model.services()[i].id() === serviceId) {
						model.services()[i].ServiceInstances.Instance.push(newInstance);
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
