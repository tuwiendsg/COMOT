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
				nav : false
			}, {
				route : 'services/:serviceId/instances/:instanceId',
				title : 'Service Instances',
				moduleId : 'instance',
				nav : false
			}, {
				route : 'services/:serviceId/instances/:instanceId/history',
				title : 'History',
				moduleId : 'history',
				nav : false
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