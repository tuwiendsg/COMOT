//define(function(require) {
//	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), router = require('plugins/router'), PNotify = require('pnotify');
define([ 'durandal/app', 'knockout', 'komapping', 'comot_client','plugins/router', 'pnotify' ], function(app, ko, komapping, comot, router, PNotify) {


	var childRouter = router.createChildRouter().makeRelative({
		moduleId : 'details',
		fromParent : true,
	// dynamicHash : ':serviceId' manage/structure
	}).map([ {
		route : 'structure/:serviceId',
		moduleId : 'structure',
		title : 'Structure',
		nav : true,
		hash : 'structure'
	}, {
		route : 'monitoring/:serviceId',
		moduleId : 'monitoring',
		title : 'Monitoring',
		nav : true,
		hash : 'monitoring'
	} ]).buildNavigationModel();

	var model = {

		activate : comot.getServices(processResult),
		services : ko.observableArray(),
		switchMonitoring : switchMonitoring,
		switchControl : switchControl,
		deployment : deployment,
		selectedServiceId : ko.observable(""),
		assignSelected : function(serviceId) {

			if (serviceId === null || typeof serviceId === 'undefined') {
				return;
			}
			console.log("id: " + serviceId);
			this.selectedServiceId(serviceId);
			router.navigate('#manage/structure/' + serviceId);
		},
		router : childRouter
	}

	return model;

	function switchMonitoring() {
		this.monitoring(!this.monitoring());

		app.showMessage('This is a message.', 'Title');
	}

	function switchControl() {
		this.control(!this.control());


		notify();
		notify("error");
		notify("info");
		notify("success");
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
