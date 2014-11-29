define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client'), router = require('plugins/router');

	
	var model = {
			activate : function(){
				console.log("Oh my! Test module activated");
			},
			something : ko.observable("wwwwwwww"),
			someFunction : function(){
				console.log("I am nicely packaged function");
			},
			deactivate : function(){
				console.log("See you on the other side");
			},
	};
	
	console.log("Test module");
	
	return model;
});