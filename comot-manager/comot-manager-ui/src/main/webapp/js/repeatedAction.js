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

			if (typeof tempId === 'undefined') {
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
