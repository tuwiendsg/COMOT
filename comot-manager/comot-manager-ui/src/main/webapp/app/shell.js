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
			}, {
				route : 'eps',
				title : 'Dynamic EPS',
				moduleId : 'eps',
				nav : true
			}, {
				route : 'services/:serviceId',
				title : 'Service Instance',
				moduleId : 'instance',
				nav : false
			}, {
				route : 'services/:serviceId/history',
				title : 'History',
				moduleId : 'history',
				nav : false
			}, {
				route : 'services/:serviceId/analysis*details',
				title : 'Analysis',
				moduleId : 'analysis',
				nav : false,
				hash : '#services/:serviceId/analysis'
			}
			// , {
			// route : 'new',
			// title : 'New',
			// moduleId : 'new',
			// nav : true
			// }
			]).buildNavigationModel();

			return router.activate();
		},
		attached : function() {
			var $loading = $('#ajaxLoading').hide();
			$(document).ajaxStart(function() {
				$loading.show();
			}).ajaxStop(function() {
				$loading.hide();
			});
		}
	};
});