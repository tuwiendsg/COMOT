define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), dimple = require('dimple'), JsonHuman = require('json_human'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var repeaterModule = require('repeater');
	var moduleTime = require('analysis/times');
	var moduleActions = require('analysis/actions');
	var notify = require('notify');

	var model = {
		// properties
		serviceId : ko.observable(""),
		instanceId : ko.observable(""),
		tabs : ko.observableArray([ {
			name : 'Elastic actions',
			module : 'analysis/actions',
			show : ko.observable(true),
		}, {
			name : 'Deployment times',
			module : 'analysis/times',
			show : ko.observable(false),
		} ]),
		// functions
		showTab : showTab,
		// life-cycle
		activate : function(serviceId, instanceId) {
			model.serviceId(serviceId);
			model.instanceId(instanceId);
		},
		attached : function() {
		},
		detached : function() {
		}
	};

	function showTab(toBeActivated) {

		for (var i = 0; i < model.tabs().length; i++) {
			var tab = model.tabs()[i];

			if (tab.name === toBeActivated.name) {
				tab.show(true);
			} else {
				tab.show(false);
			}
		}
	}

	return model;

});
