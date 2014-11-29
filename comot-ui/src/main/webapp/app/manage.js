define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), router = require('plugins/router'), PNotify = require('pnotify');

	var moduleStructure = require('details/structure');
	var moduleMonitoring = require('details/monitoring');

	var model = {

		activate : function() {
			console.log("activating");
			comot.getServices(processResult),

			activateTab(model.tabs()[0]);
		},
		services : ko.observableArray(),
		switchMonitoring : switchMonitoring,
		switchControl : switchControl,
		deployment : deployment,
		selectedServiceId : ko.observable(),
		assignSelected : function(serviceId) {

			if (serviceId === null || typeof serviceId === 'undefined') {
				return;
			}
			console.log("id: " + serviceId);
			this.selectedServiceId(serviceId);

			activateTab(model.tabs()[0]);
		},

		tabs : ko.observableArray([ {
			name : 'Structure',
			module : 'details/structure',
			show : ko.observable(false),
			instance : moduleStructure
		}, {
			name : 'Monitoring',
			module : 'details/monitoring',
			show : ko.observable(false),
			instance : moduleMonitoring
		} ]),

		activateTab : function() {
			activateTab(this);
		}
	}

	function activateTab(toBeActivated) {

		for (var i = 0; i < model.tabs().length; i++) {
			var tab = model.tabs()[i];

			if (tab.name === toBeActivated.name) {
				console.log("activating tab" + tab.name)
				tab.instance.startTab(model.selectedServiceId);
				tab.show(true);
			} else {
				console.log("deactivating tab" + tab.name)
				tab.instance.stopTab();
				tab.show(false);
			}
		}
	}

	return model;

	function switchMonitoring() {

		if (this.monitoring()) {
			comot.stopMonitoring(this.id(), notify('success'))
		} else {
			comot.startMonitoring(this.id(), notify('success'))
		}

		this.monitoring(!this.monitoring());
	}

	function switchControl() {

		notify();
		notify("error");
		notify("info");
		notify("success");

		this.control(!this.control());
	}

	function deployment() {

		this.deployment(!this.deployment());
	}

	function processResult(data) {
		model.services = komapping.fromJS(data);

	}

	function notify(type) {
		var opts = {
			title : "Over Here",
			text : "Check me out. I'm in a different stack.",
			styling : 'bootstrap3',
		};
		switch (type) {
		case 'error':
			opts.title = "Oh No";
			opts.text = "Watch out for that water tower!";
			opts.type = "error";
			break;
		case 'info':
			opts.title = "Breaking News";
			opts.text = "Have you met Ted?";
			opts.type = "info";
			break;
		case 'success':
			opts.title = "Good News Everyone";
			opts.text = "I've invented a device that bites shiny metal asses.";
			opts.type = "success";
			break;
		}
		new PNotify(opts);
	}

});
