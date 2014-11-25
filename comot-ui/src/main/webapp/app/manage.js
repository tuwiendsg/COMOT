define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), komapping = require('komapping'), comot = require('comot_client'), router = require('plugins/router');

	var childRouter = router.createChildRouter().makeRelative({
		moduleId : 'details',
		fromParent : true,
	// dynamicHash : ':serviceId' manage/structure
	}).map([  {
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

		attached : comot.getServices(processResult),
		services : ko.observableArray(),
		switchMonitoring : switchMonitoring,
		switchControl : switchControl,
		deployment : deployment,
		selectedServiceId : ko.observable(""),
		assignSelected : function(serviceId) {
			
			if (serviceId === null ||typeof serviceId === 'undefined') {
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
