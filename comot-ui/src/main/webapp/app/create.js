define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), comot = require('comot_client'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {
		checkboxDepl : ko.observable(true),
		checkboxMoni : ko.observable(),
		checkboxCont : ko.observable(),
		tosca : ko.observable(""),
		mcr : ko.observable(""),
		effects : ko.observable(""),
		deploy : deploy
	}

	function deploy() {
		var tosca = this.tosca();
		var mcr = this.mcr();
		var effects = this.effects();

		if (model.checkboxDepl() === true) {// deploy
			$.when(comot.createAndDeploy(model.tosca(), "Cloud service deployed.", function(request) {
				notify.error(comot.errorBody(request.responseText));
			})).done(
					function(result) {
						
						router.navigate('#manager');
						
						if (model.checkboxMoni() === true) {// start monitoring
							$.when(comot.startMonitoring(result, function() {
								notify.success("Monitoring started for " + result);
							}, "Failed to start monitoring for " + result)).done(
									function(result2) {

										if (model.mcr() !== "") { // set MCR
											comot.createMcr(result, model.mcr(), "Applied MCR for " + result,
													"Failed to applie MCR for " + result);
										}
									})
						}

						if (model.checkboxCont() === true) { // start control
							console.log("controllll")
						}
					});

		} else {
			notify.info("To create new service insert TOSCA deployment description.");
		}

	}

	return model;
});
