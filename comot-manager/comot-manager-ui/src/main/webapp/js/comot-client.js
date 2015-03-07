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
	var recordings = base + "recordings/";
	var instances = "/instances/";

	exports.eventPath = function(serviceId, instanceId) {
		return services + serviceId + instances + instanceId + "/events";
	}

	function getRequestCore(onSuccess, onError) {

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
				console.log("status: " + request.status + "(" + request.statusText + "), " + errorBody(error));
			}
		} else if (typeof onError === 'string') {
			core.error = function(request, status, error) { // $.parseJSON( jsonString )
				notify.error(onError);
			}
		}
		return core;
	}

	function isFunction(functionToCheck) {
		var getType = {};
		return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
	}

	function errorBody(error) {
		var msg = "";

		if (typeof onError === 'string') {
			var obj = $.parseJSON(error)

			if (typeof obj.origin !== 'undefined') {
				msg = msg + "component: " + obj.origin
			}
			if (typeof obj.message !== 'undefined') {
				if (msg !== "") {
					msg = msg + "\n";
				}
				msg = msg + "message: " + obj.message
			}
		} else {
			msg = error;
		}
		return msg;
	}

	exports.errorBody = function(error) {
		return errorBody(error);
	}

	exports.base = function() {
		return errorBody(error);
	}

	exports.baseServices = function() {
		return errorBody(error);
	}

	// API

	exports.createService = function(tosca, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services;
		request.data = tosca;
		request.contentType = "application/xml";
		return $.ajax(request);
	}

	exports.createServiceInstance = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services + serviceId + instances;
		return $.ajax(request);
	}

	exports.startServiceInstance = function(serviceId, instanceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + instances + instanceId + "/start";
		return $.ajax(request);
	}

	exports.stopServiceInstance = function(serviceId, instanceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + instances + instanceId + "/stop";
		return $.ajax(request);
	}

	exports.assignSupportingEps = function(serviceId, instanceId, epsId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + instances + instanceId + "/eps/" + epsId;
		return $.ajax(request);
	}

	exports.removeSupportingEps = function(serviceId, instanceId, epsId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + instances + instanceId + "/eps/" + epsId;
		return $.ajax(request);
	}

	// GET

	exports.getServices = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services;
		return $.ajax(request);
	}

	exports.getServiceInstance = function(serviceId, instanceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + serviceId + instances + instanceId;
		return $.ajax(request);
	}

	exports.lifecycle = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + "lifecycle";
		return $.ajax(request);
	}

	exports.getEps = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + "eps";
		return $.ajax(request);
	}

	exports.getAllInstances = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + "allInstances";
		return $.ajax(request);
	}

	// //////////////////////////////// REVISIONS

	exports.getRecording = function(csInstanceId, objectId, timestamp, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + csInstanceId + "/objects/" + objectId + "/" + timestamp;
		return $.ajax(request);
	}

	exports.getEvents = function(csInstanceId, objectId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + csInstanceId + "/objects/" + objectId + "/events";
		return $.ajax(request);
	}

	exports.getObjects = function(csInstanceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = recordings + csInstanceId + "/objects";
		return $.ajax(request);
	}
}));