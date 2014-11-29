define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client'), router = require('plugins/router');

	var model = {
		startTab : function() {
			model.tabIsActive = true;
			console.log("monitoring activate")
		},
		detached : stopTab,
		stopTab : stopTab,
		tabIsActive : false,
	};

	function stopTab(){
		model.tabIsActive = false;
		console.log("monitoring stopped")
	}
	
	console.log("monitoring");

	return model;
});