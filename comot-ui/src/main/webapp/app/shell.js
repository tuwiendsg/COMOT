define(function(require) {
	var router = require('plugins/router');

	return {
		router : router,
		activate : function() {
			router.map([ {
				route : '',
				title : 'Home',
				moduleId : 'home',
				nav : false
			}, {
				route : 'create',
				title : 'Create',
				moduleId : 'create',
				nav : true
			}, {
				route : 'view',
				title : 'View',
				moduleId : 'view',
				nav : true
			}, {
				route : 'tree',
				title : 'd3',
				moduleId : 'tree',
				nav : true
			}]).buildNavigationModel();

			return router.activate();
		}
	};
});