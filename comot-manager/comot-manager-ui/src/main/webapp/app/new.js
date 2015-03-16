define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {

		lifecycle : {},
		serviceId : ko.observable(),
		detached : function() {
			
		},
		deactivate : function() {
			
		},
		attached : function() {

		}
	}

	return model;

	
});
