(function(factory) {
	// require stuff taken from here
	// https://github.com/SteveSanderson/knockout.mapping/blob/master/build/output/knockout.mapping-latest.debug.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
		factory(require("jquery"), exports);
	} else if (typeof define === "function" && define["amd"]) {
		define([ "jquery", "exports" ], factory);
	} else {
		console.log("What just happened?");
		// factory(ko, ko.mapping = {});
	}

}(function($, exports) {

	var base = "rest/";
	var services = base + "services/";

	exports.deploy = function(tosca, processResult) {
		$.ajax({
			type : "POST",
			url : services,
			data : tosca,
			dataType : "xml",
			contentType : "application/xml",
			success : processResult
		});
	}

	exports.startMonitoring = function(serviceId, processResult) {

		console.log("aaaaa");

		$.ajax({
			type : "PUT",
			url : services + serviceId + "/monitoring",
			success : processResult
		});
	}

	exports.startControl = function(serviceId, processResult) {

		console.log(mcr);

		$.ajax({
			type : "PUT",
			url : services + serviceId + "/control",
			success : processResult
		});
	}

	exports.getServices = function(processResult) {
		$.ajax({
			type : "GET",
			url : services,
			dataType : "json",
			success : processResult
		});
	}

	exports.checkStatus = function(serviceId, processResult) {

		$.ajax({
			type : "GET",
			url : services + serviceId + "/state",
			dataType : "json",
			success : processResult
		});
	}

}));