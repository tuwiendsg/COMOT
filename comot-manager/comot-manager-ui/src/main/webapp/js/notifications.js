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