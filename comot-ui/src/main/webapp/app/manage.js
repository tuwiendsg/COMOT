define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), router = require('plugins/router'), PNotify = require('pnotify');

	var moduleStructure = require('details/structure');
	var moduleMonitoring = require('details/monitoring');
	var notify = require('notify');

	var activeTabIndex = 0;

	var model = {

		activate : function() {
			console.log("activated");
			comot.getServices(processResult),
			activateTab(model.tabs()[activeTabIndex]);
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

			this.selectedServiceId(serviceId);

			activateTab(model.tabs()[activeTabIndex]);
		},
		tabs : ko.observableArray([ {
			name : 'Structure',
			module : 'details/structure',
			show : ko.observable(true),
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
				tab.instance.startTab(model.selectedServiceId());
				tab.show(true);
				activeTabIndex = i;
			} else {
				tab.instance.stopTab();
				tab.show(false);
			}
		}
	}

	return model;

	function switchMonitoring() {
		var that = this;

		if (this.monitoring()) {
			comot.stopMonitoring(this.id(), function() {
				that.monitoring(false);
				notify.success("Monitoring stopped for "+that.id());
			}, "Failed to stop monitoring for "+that.id())
		} else {
			comot.startMonitoring(this.id(), function() {
				that.monitoring(true);
				notify.success("Monitoring started for "+that.id());
			}, "Failed to start monitoring for "+that.id())
		}
	}

	function switchControl() {

		this.control(!this.control());
	}

	function deployment() {

		this.deployment(!this.deployment());
	}

	function processResult(data) {
		model.services = komapping.fromJS(data);

	}

});
