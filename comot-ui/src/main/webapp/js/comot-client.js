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

	function getRequestCore(onSuccess, onError) {

		var core = {};

		console.log("onSuccess aa" + typeof onSuccess);
		console.log("onError " + typeof onError);

		// SUCCESS
		if (isFunction(onSuccess)) {
			console.log("isFuncion")
			core.success = onSuccess;
		} else if (onSuccess === null || typeof onSuccess === 'undefined') {
			core.success = function() {
				console.log("success");
			}
		} else if (typeof onSuccess === 'string') {
			core.success = function() {
				notify.success(onSuccess);
			}
		}

		// ERROR
		if (isFunction(onError)) {
			core.success = onError;
		} else if (onError === null || typeof onError === 'undefined') {
			core.error = function() {
				console.log("error");
			}
		} else if (typeof onError === 'string') {
			core.error = function() {
				notify.error(onError);
			}
		}
		return core;
	}

	function isFunction(functionToCheck) {
		var getType = {};
		return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
	}

	exports.deploy = function(tosca, onSuccess, onError) {
		$.ajax({
			type : "POST",
			url : services,
			data : tosca,
			dataType : "xml",
			contentType : "application/xml",
			success : onSuccess,
			error : onError
		});
	}

	exports.startMonitoring = function(serviceId, onSuccess, onError) {
	
		var request = getRequestCore(onSuccess, onError);
		request.type = "PUT";
		request.url = services + serviceId + "/monitoring";
		$.ajax(request);
	}

	exports.startControl = function(serviceId, onSuccess, onError) {

		console.log(mcr);

		$.ajax({
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
		$.ajax(request);
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

}));