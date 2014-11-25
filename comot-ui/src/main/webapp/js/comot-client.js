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

	exports.getServices = function(processResult) {
		$.ajax({
			type : "GET",
			url : "rest/services",
			dataType : "json",
			success : processResult
		});
	}

	exports.checkStatus = function(serviceId, processResult) {
		$.ajax({
			type : "GET",
			url : "rest/services/" + serviceId + "/status",
			dataType : "json",
			success : processResult
		});
	}
	
	
	exports.deploy = function(tosca, processResult) {
	$.ajax({
		type : "POST",
		url : "rest/services",
		data : tosca,
		dataType : "xml",
		contentType : "application/xml",
		success : processResult
	});
}

}));