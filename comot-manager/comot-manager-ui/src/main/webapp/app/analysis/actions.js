define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), dimple = require('dimple'), JsonHuman = require('json_human'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var notify = require('notify');

	var model = {
		// properties
		serviceId : ko.observable(""),
		actionPlans : ko.observableArray(),
		// functions

		// life-cycle
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {

			comot.getElasticActions(model.serviceId(), function(data) {

				// var viewData = $.extend(true, {}, data);

				for (var i = 0; i < data.length; i++) {
					var one = data[i];
					one.timestamp = utils.longToDateString(one.timestamp);

					delete data[i].changeTimestamp;

					for (var j = 0; j < one.actions.length; j++) {
						one.actions[j].timestamp = utils.longToDateString(one.actions[j].timestamp);
					}

					var human = JsonHuman.format(one);
					var tmp = document.createElement("div");
					tmp.appendChild(human);

					one.html = tmp.innerHTML;
				}

				model.actionPlans(data)
			});
		},
		detached : function() {

		}
	};

	return model;

});
