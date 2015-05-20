/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
				route : 'eps',
				title : 'User-managed EPS',
				moduleId : 'eps',
				nav : true
			}, {
				route : 'services/:serviceId',
				title : 'Cloud Service',
				moduleId : 'instance',
				nav : false
			}, {
				route : 'services/:serviceId/history',
				title : 'Event Log',
				moduleId : 'history',
				nav : false
			}, {
				route : 'services/:serviceId/analysis*details',
				title : 'Analysis',
				moduleId : 'analysis',
				nav : false,
				hash : '#services/:serviceId/analysis'
			}
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