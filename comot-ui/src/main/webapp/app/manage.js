define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), router = require('plugins/router'), PNotify = require('pnotify'), bootstrap = require('bootstrap');

	var moduleStructure = require('details/structure');
	var moduleMonitoring = require('details/monitoring');
	var moduleRevisions = require('details/revisions');
	var notify = require('notify');
	var repeater = require('repeater');

	var activeTabIndex = 0;
	var repeated = repeater.create("State_List", 7000);

	var model = {
		activate : function() {
			repeated.runWith(null, function() {
				comot.getServices(processResult)
			})
			activateTab(model.tabs()[activeTabIndex]);
		},
		detached : function() {
			repeated.stop();
		},
		services : ko.observableArray(),
		switchMonitoring : switchMonitoring,
		switchControl : switchControl,
		switchRecording : switchRecording,
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
		},{
			name : 'Revisions',
			module : 'details/revisions',
			show : ko.observable(false),
			instance : moduleRevisions
		} ]),

		activateTab : function() {
			activateTab(this);
		},
		mcr : ko.observable(),
		getMcr : function(id){
			comot.getMcr(id, function(data){
				model.mcr(data);
			});
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
				notify.success("Monitoring stopped for " + that.id());
			}, "Failed to stop monitoring for " + that.id())
		} else {
			comot.startMonitoring(this.id(), function() {
				that.monitoring(true);
				notify.success("Monitoring started for " + that.id());
			}, "Failed to start monitoring for " + that.id())
		}
	}
	
	function switchRecording() {
		var that = this;

		if (this.recording()) {
			comot.stopRecording(this.id(), function() {
				that.recording(false);
				notify.success("Recording stopped for " + that.id());
			}, "Failed to stop recording for " + that.id());
		} else {
			comot.startRecording(this.id(), function() {
				that.recording(true);
				notify.success("Recording started for " + that.id());
			}, "Failed to start recording for " + that.id());
		}
	}

	function switchControl() {

		this.control(!this.control());
	}

	function deployment() {

		var that = this;

		if (this.deployment()) {

			app.showMessage("Confirm the undeployment of service '" + that.id() + "'. This may take several minutes.",
					"Undeployment", [ {
						text : "Confirm",
						value : true
					}, {
						text : "Cancel",
						value : false
					} ], false).then(function(dialogResult) {

				if (dialogResult) {
					comot.undeploy(that.id(), function() {
						that.deployment(false);
						notify.success("Cloud service " + that.id() + " undeployed.");
					}, "Failed to stop monitoring for " + that.id())
				}
			});

		} else {

			app.showMessage("Confirm the deployment of service '" + that.id() + "'. This may take several minutes.",
					"Deployment", [ {
						text : "Confirm",
						value : true
					}, {
						text : "Cancel",
						value : false
					} ], false).then(function(dialogResult) {

				if (dialogResult) {
					comot.deploy(that.id(), function() {
						that.deployment(true);
						notify.success("Cloud service " + that.id() + " deployed.")
					}, function(request) {
						notify.error(comot.errorBody(request.responseText));
					})
				}
			});
		}
	}

	function processResult(data) {
		model.services = komapping.fromJS(data);

	}

});
