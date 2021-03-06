/***********************************************************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 * 
 * This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8
 * \#317790)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
(function(factory) {
	// require stuff taken from here
	// https://github.com/SteveSanderson/knockout.mapping/blob/master/build/output/knockout.mapping-latest.debug.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
		factory(require("jquery"), require('notify'), exports);
	} else if (typeof define === "function" && define["amd"]) {
		define([ "jquery", "notify", "exports" ], factory);
	} else {
		console.log("What just happened?");
		// factory(ko, ko.mapping = {});
	}

}(function($, notify, exports) {

	var base = "rest/";
	var services = base + "services/";
	var templates = base + "templates/";
	var eps = base + "eps/";
	var recordings = base + "recordings/";
	var instances = "/instances/";

	exports.eventPath = function(serviceId) {
		return services + serviceId + "/events";
	}

	function getRequestCore(onSuccess, onError, spinner) {

		var core = {};

		// SUCCESS
		if (isFunction(onSuccess)) {
			core.success = onSuccess;
		} else if (onSuccess === null || typeof onSuccess === 'undefined') {
			core.success = function(data) {
				console.log("success: " + data);
			}
		} else if (typeof onSuccess === 'string') {
			core.success = function() {
				notify.success(onSuccess);
			}
		}

		// ERROR
		if (isFunction(onError)) {
			core.error = onError;

		} else if (onError === null || typeof onError === 'undefined') {
			core.error = function(request, status, error) {

				if (request.status == 422) {
					notify.warning(request.responseText);
				} else {
					notify.error("Failed. \n Reason: " + request.status + " - " + request.statusText + ", "
							+ request.responseText);
				}
			}
		} else if (typeof onError === 'string') {
			core.error = function(request, status, error) {

				if (request.responseText === "") {
					notify.error(onError);
				} else {
					if (request.status == 422) {
						notify.warning(onError + "\n Reason: " + request.responseText);
					} else {
						notify.error(onError + "\n Reason: " + request.responseText);
					}
				}
			}
		}

		if (typeof spinner !== 'undefined') {
			core.beforeSend = function() {
				if ($(spinner).length) {
					$(spinner).show();
				}
			};
			core.complete = function() {
				if ($(spinner).length) {
					$(spinner).hide();
				}
			};
		}
		return core;
	}

	function isFunction(functionToCheck) {
		var getType = {};
		return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
	}

	// API

	exports.createTemplateTosca = function(tosca, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = templates + "tosca";
		request.data = tosca;
		request.contentType = "application/xml";
		return $.ajax(request);
	}

	exports.removeTemplate = function(templateId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = templates + templateId;
		return $.ajax(request);
	}

	exports.createServiceTosca = function(tosca, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services + "tosca";
		request.data = tosca;
		request.contentType = "application/xml";
		return $.ajax(request);
	}

	exports.createService = function(service, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services;
		request.data = JSON.stringify(service);
		request.contentType = "application/json";
		return $.ajax(request);
	}

	exports.createServiceFromTemplate = function(templateId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services + "template";
		request.data = templateId;
		request.contentType = "text/plain";
		return $.ajax(request);
	}

	exports.startService = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/active";
		return $.ajax(request);
	}

	exports.stopService = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + "/active";
		return $.ajax(request);
	}

	exports.killService = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/terminate";
		return $.ajax(request);
	}

	exports.removeService = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId;
		return $.ajax(request);
	}

	exports.assignSupportingEps = function(serviceId, epsId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/eps/" + epsId;
		return $.ajax(request);
	}

	exports.removeSupportingEps = function(serviceId, epsId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + "/eps/" + epsId;
		return $.ajax(request);
	}

	exports.triggerCustomEvent = function(serviceId, epsId, eventName, optionalMesage, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.data = optionalMesage;
		request.contentType = "text/plain";
		request.url = services + serviceId + "/eps/" + epsId + "/events/" + eventName;
		return $.ajax(request);
	}

	exports.createDynamicEps = function(epsId, formData, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.data = formData;
		request.contentType = false; // "multipart/form-data";
		request.processData = false;
		request.url = eps + epsId + "/instances";
		return $.ajax(request);
	}

	exports.removeDynamicEps = function(epsId, epsInstanceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = eps + epsId + "/instances/" + epsInstanceId;
		return $.ajax(request);
	}

	exports.reconfigureElasticity = function(serviceId, service, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.data = JSON.stringify(service);
		request.contentType = "application/json";
		request.url = services + serviceId + "/elasticity";
		return $.ajax(request);
	}

	// GET

	exports.getTemplatesNonEps = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.data = {
			type : "NON_EPS"
		};
		request.url = templates;
		return $.ajax(request);
	}

	exports.getServicesNonEps = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.data = {
			type : "NON_EPS"
		};
		request.url = services;
		return $.ajax(request);
	}

	exports.getService = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.url = services + serviceId;
		return $.ajax(request);
	}

	exports.lifecycle = function(level, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.url = services + "lifecycle/" + level;
		return $.ajax(request);
	}

	exports.getEps = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.url = eps;
		return $.ajax(request);
	}

	exports.getEpsDynamic = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.data = {
			type : "USER_MANAGED"
		};
		request.url = eps;
		return $.ajax(request);
	}

	exports.getEpsInstancesActive = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.data = {
			type : "ACTIVE"
		};
		request.url = eps + "instances";
		return $.ajax(request);
	}

	exports.getEpsInstancesDynamic = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json";
		request.data = {
			type : "USER_MANAGED"
		};
		request.url = eps + "instances";
		return $.ajax(request);
	}

	// //////////////////////////////// RECORDER

	exports.getRecording = function(serviceId, objectId, timestamp, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/objects/" + objectId + "/" + timestamp;

		return $.ajax(request);
	}

	exports.getEventsThanModifiedObject = function(serviceId, objectId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/objects/" + objectId + "/events";
		return $.ajax(request);
	}

	exports.getAllEvents = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/events";
		return $.ajax(request);
	}

	exports.getObjects = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/objects";
		return $.ajax(request);
	}

	exports.getUnitInstanceDeploymentEvents = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError, '#spinnerGetUnitInstanceDeploymentEvents');
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/analytics/unitInstanceDeploymentEvents";
		return $.ajax(request);
	}

	exports.getElasticActions = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError, '#spinnerGetElasticActions');
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + serviceId + "/analytics/elasticActions";
		return $.ajax(request);
	}
}));