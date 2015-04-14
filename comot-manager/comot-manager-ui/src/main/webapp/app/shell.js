define(function(require) {
	var router = require('plugins/router'), bootstrap = require('bootstrap');

	return {
		router : router,
		activate : function() {
			router.map([ {
				route : '',
				title : 'Home',
				moduleId : 'services',
				nav : false
			}, {
				route : 'services',
				title : 'Cloud Services',
				moduleId : 'services',
				nav : true
			},{
				route : 'eps',
				title : 'Dynamic EPS',
				moduleId : 'eps',
				nav : true
			}, {
				route : 'services/:serviceId/instances/:instanceId',
				title : 'Service Instance',
				moduleId : 'instance',
				nav : false
			}, {
				route : 'services/:serviceId/instances/:instanceId/history',
				title : 'History',
				moduleId : 'history',
				nav : false
			},{
				route : 'services/:serviceId/instances/:instanceId/analysis*details',
				title : 'Analysis',
				moduleId : 'analysis',
				nav : false,
				hash: '#services/:serviceId/instances/:instanceId/analysis'
			}
			// , {
			// route : 'new',
			// title : 'New',
			// moduleId : 'new',
			// nav : true
			// }
			]).buildNavigationModel();

			return router.activate();
		}
	};
});