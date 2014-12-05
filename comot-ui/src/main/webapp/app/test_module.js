define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), $ = require("jquery"), comot = require('comot_client'), router = require('plugins/router');

	var bootstrap = require('bootstrap')
	
	var model = {
			activate : function(){
				console.log("Oh my! Test module activated");
			},
			something : ko.observable("wwwwwwww"),
			someFunction : function(){
				console.log("I am nicely packaged function");
				
				//console.log($("#myModal"));
				
				// $("#myModal").show();//modal('show');
				// $('#myModal').modal('show');
			},
			deactivate : function(){
				console.log("See you on the other side");
			},
	};
	
	console.log("Test module");
	
	return model;
});