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
	var revisions = "/revisions";

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

	exports.createAndDeploy = function(tosca, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "POST";
		request.url = services;
		request.data = tosca;
		// request.dataType = "json"
		request.contentType = "application/xml";
		return $.ajax(request);
	}

	exports.deploy = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/deployment";
		return $.ajax(request);
	}

	exports.undeploy = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + "/deployment";
		return $.ajax(request);
	}

	exports.startMonitoring = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/monitoring";
		return $.ajax(request);
	}

	exports.startControl = function(serviceId, onSuccess, onError) {

		console.log(mcr);

		return $.ajax({
			type : "PUT",
			url : services + serviceId + "/control",
			success : onSuccess,
			error : onError
		});
	}

	exports.stopMonitoring = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + "/monitoring";
		return $.ajax(request);
	}

	exports.stopControl = function(serviceId, onSuccess, onError) {

		console.log(mcr);

		$.ajax({
			type : "DELETE",
			url : services + serviceId + "/control",
			success : onSuccess,
			error : onError
		});
	}

	exports.createMcr = function(serviceId, mcr, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/mcr";
		request.data = mcr;
		request.contentType = "application/xml";
		return $.ajax(request);
	}

	exports.getMcr = function(serviceId, onSuccess, onError) {
		$.ajax({
			type : "GET",
			url : services + serviceId + "/mcr",
			dataType : "text",
			success : onSuccess,
			error : onError
		});
	}

	exports.getServices = function(onSuccess, onError) {
		$.ajax({
			type : "GET",
			url : services,
			dataType : "json",
			success : onSuccess,
			error : onError
		});
	}

	exports.checkStatus = function(serviceId, onSuccess, onError) {

		$.ajax({
			type : "GET",
			url : services + serviceId + "/state",
			dataType : "json",
			success : onSuccess,
			error : onError
		});
	}

	exports.monitoringData = function(serviceId, onSuccess, onError) {

		$.ajax({
			type : "GET",
			url : services + serviceId + "/monitoring/snapshots/last",
			dataType : "json",
			success : onSuccess,
			error : onError
		});
	}

	// //////////////////////////////// LIFECYCLE
	exports.eventPath = function(serviceId, instanceId) {
		return services + serviceId + "/instances/" + instanceId+"/events";
	}

	exports.lifecycle = function(onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + "lifecycle";
		return $.ajax(request);
	}

	// //////////////////////////////// REVISIONS

	exports.startRecording = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + revisions + "/recording";
		return $.ajax(request);
	}

	exports.stopRecording = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + revisions + "/recording";
		return $.ajax(request);
	}

	exports.stopRecordingAndDeleteData = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "DELETE";
		request.url = services + serviceId + revisions;
		return $.ajax(request);
	}

	exports.getRevision = function(serviceId, objectId, timestamp, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + serviceId + revisions + "/" + objectId + "/" + timestamp;
		return $.ajax(request);
	}

	exports.getChanges = function(serviceId, objectId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + serviceId + revisions + "/changes/" + objectId;
		return $.ajax(request);
	}

	exports.getObjects = function(serviceId, onSuccess, onError) {

		var request = getRequestCore(onSuccess, onError);
		request.type = "GET";
		request.dataType = "json"
		request.url = services + serviceId + revisions + "/objects";
		return $.ajax(request);
	}
}));