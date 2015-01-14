define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client');
	var repeater = require('repeater');

	var tab = repeater.create("Monitoring", 7000);

	var model = {
		startTab : function(serviceId) {
			tab.runWith(serviceId, function() {
				comot.getLastRevision(serviceId, processResopnse)
			})
		},
		stopTab : function() {
			tab.stop();
		},
		detached : function() {
			model.stopTab();
		},
	};

	return model;

	function processResopnse(data) {

		$("#output_revisions").html(JsonHuman.format(data));
		
	}
	
	
});
