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

	exports.longToDateString = function(long) {
		var date = new Date(long);
		var string = "" + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear() + " "
				+ (date.getHours() + 1) + ":" + date.getMinutes() + ":" + date.getSeconds();
		return string;
	}
	
}));