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
				route : 'create',
				title : 'Create',
				moduleId : 'create',
				nav : true
			},{
				route : 'new',
				title : 'New',
				moduleId : 'new',
				nav : true
			},{
				route : 'services',
				title : 'Cloud Services',
				moduleId : 'services',
				nav : true
			}, {
				route : 'instance',
				title : 'Service Instances',
				moduleId : 'instance',
				nav : true
			}, {
				route : 'manage*details',
				title : 'Manage',
				moduleId : 'manage',
				hash : '#manage',
				nav : true
			} ]).buildNavigationModel();

			return router.activate();
		}
	};
});