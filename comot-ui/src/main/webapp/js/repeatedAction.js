(function(factory) {
	// require stuff taken from here
	// https://github.com/SteveSanderson/knockout.mapping/blob/master/build/output/knockout.mapping-latest.debug.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
		factory(exports);
	} else if (typeof define === "function" && define["amd"]) {
		define([ "exports" ], factory);
	} else {
		console.log("What just happened?");
		// factory(ko, ko.mapping = {});
	}

}(function(exports) {

	exports.create = function(name, timeout) {
		return new Repeater(name, timeout);
	}

	function Repeater(name, timeout) {

		var name = name;
		var timeout = timeout;
		var tId = undefined;
		var isActive = false;
		var lastId = "";

		/**
		 * Start repeater. If called second time, the call has effect only if the input is different. Executes the
		 * functionTocall repeatedly until stop() is called.
		 */
		this.runWith = function(input, functionTocall) {
			console.log("start " + name)
			var tempId = input;

			if (tempId === null || typeof tempId === 'undefined') {
				this.stop();
				return;
			}

			if (lastId !== tempId || isActive === false) {
				clearInterval(tId);

				lastId = tempId;
				isActive = true;
				console.log("starting " + name + " " + tempId)

				functionTocall();
				tId = setInterval(function() {
					functionTocall();
				}, timeout);
			}
		}

		this.stop = function() {
			isActive = false;
			clearInterval(tId);
			console.log("stopping " + name)
		}
	}

}));
