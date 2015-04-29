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
