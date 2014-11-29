define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), comot = require('comot_client');

//	var testModule = require('test_module');
//	
//	console.log("aaa "+testModule.something());
//	
	var viewModel = {
			activate : function(){
				console.log("I am here to create");
			},
			deactivate : function(){
				testModule.deactivate();
				console.log("This is the end");
			},

		checkboxDepl : ko.observable(),
		checkboxMoni  : ko.observable(),
		checkboxCont  : ko.observable(),

		tosca  : ko.observable(""),
		mcr  : ko.observable(),
		effects  : ko.observable(),

		deploy  : function() {
			var tosca = this.tosca();
			var mcr = this.mcr();
			var effects = this.effects();

			console.log("Tosca: " + tosca);

			comot.deploy(this.tosca(), function(data) {
				console.log(data);
			});
		}
	}

	return viewModel;
});
