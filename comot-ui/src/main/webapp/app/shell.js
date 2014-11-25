define(function(require) {
	var router = require('plugins/router');

	return {
		router : router,
		activate : function() {
			router.map([ {
				route : '',
				title : 'Home',
				moduleId : 'manage',
				nav : false
			}, {
				route : 'create',
				title : 'Create',
				moduleId : 'create',
				nav : true
			}, {
//				route : 'manage',
//				title : 'Manage',
//				moduleId : 'manage',
//				// hash: 'manage',
//				nav : true
//			}, {
//				route : 'manage(/:serviceId)*details',
//				title : 'Manage',
//				moduleId : 'manage',
//				hash : '#manage/:serviceId',
//				nav : false
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