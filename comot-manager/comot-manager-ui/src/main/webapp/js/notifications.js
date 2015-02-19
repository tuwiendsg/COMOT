(function(factory) {
	// require stuff taken from here
	// https://github.com/SteveSanderson/knockout.mapping/blob/master/build/output/knockout.mapping-latest.debug.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
		factory(require('pnotify'), exports);
	} else if (typeof define === "function" && define["amd"]) {
		define([ "pnotify", "exports" ], factory);
	} else {
		console.log("What just happened?");
		// factory(ko, ko.mapping = {});
	}

}(function(PNotify, exports) {

		
	exports.success = function(text) {
		var opts = notifyCore();
		opts.title = "Success";
		opts.text = text;
		opts.type = "success";
		new PNotify(opts);
	}

	exports.error = function(text) {
		var opts = notifyCore();
		opts.title = "Error";
		opts.text = text;
		opts.type = "error";
		new PNotify(opts);
	}
	
	exports.info = function(text) {
		var opts = notifyCore();
		opts.title = "Info";
		opts.text = text;
		opts.type = "info";
		new PNotify(opts);
	}

	function notifyCore() {
		return opts = {
			styling : 'bootstrap3',
		};
	}
	
	
}));