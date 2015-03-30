define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {
		// properties
		services : ko.observableArray(),
		// functions
		newService : newService,
		viewService : viewService,
		removeService : removeService,
		newInstance : newInstance,
		viewInstance : viewInstance,
		removeInstance : removeInstance,
		// life-cycle
		detached : function() {

		},
		deactivate : function() {

		},
		activate : function() {
			model.services.removeAll();

			comot.getEpsDynamic(function(epses) {
				comot.getEpsInstancesDynamic(function(epsesInstances) {

					var viewEpses = [];

					for (var i = 0; i < epses.length; i++) {

						var eps = epses[i];
						var instances = eps.service.ServiceInstances.Instance;
						var map = {}

						console.log(eps)

						viewEpses[i] = {
							'id' : eps.id,
							'serviceId' : eps.service.id,
							'instances' : ko.observableArray()
						};

						for (var j = 0; j < instances.length; j++) {
							map[instances[j].id] = {
								'id' : "",
								'serviceInstnaceId' : instances[j].id,
								'dateCreatedFormated' : utils.longToDateString(instances[j].dateCreated),
							};
						}

						for (var j = 0; j < epsesInstances.length; j++) {
							var oneEpsIn = epsesInstances[j];

							if (oneEpsIn.osu.id === eps.id) {
								map[oneEpsIn.serviceInstance.id].id = oneEpsIn.id;
							}
						}

						for ( var key in map) {
							viewEpses[i].instances.push(map[key]);
						}

					}
					model.services(viewEpses);
				});
			});

			// comot.getEpsInstancesDynamic(function(instances) {
			//
			// var map = {}
			//
			// for (var i = 0; i < instances.length; i++) {
			// map[instances[i].osu.id] = instances[i].osu;
			// }
			//
			// for ( var osuId in map) {
			//
			// var viewInstances = [];
			//
			// for (var j = 0; j < instances.length; j++) {
			// viewInstances[j] = {
			// 'id' : instances[j].id,
			// 'serviceInstnaceId' : instances[j].serviceInstance.id,
			// 'dateCreatedFormated' : utils.longToDateString(instances[j].serviceInstance.dateCreated),
			// };
			// }
			//
			// model.services.push({
			// 'id' : osuId,
			// 'serviceId' : map[osuId].service.id,
			// 'instances' : ko.observableArray(viewInstances)
			// });
			//
			// }
			// })
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

	function removeService(serviceId) {

	}

	function newInstance(epsId) {

		comot.createDynamicEps(epsId, function(data) {
			var serviceInstanceId = data;
			var newInstance = {
				'id' : "",
				'serviceInstnaceId' : serviceInstanceId,
				'dateCreatedFormated' : ""
			}

			for (var i = 0; i < model.services().length; i++) {
				if (model.services()[i].id === epsId) {
					// newInstance.dateCreatedFormated = utils.longToDateString(newInstance.dateCreated);
					model.services()[i].instances.push(newInstance);
					break;
				}
			}

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
