define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), comot = require('comot_client');

	function AppViewModel() {

		this.checkboxDepl = ko.observable();
		this.checkboxMoni = ko.observable();
		this.checkboxCont = ko.observable();

		this.tosca = ko.observable("");
		this.mcr = ko.observable();
		this.effects = ko.observable();

		this.deploy = function() {

			var tosca = this.tosca();
			var mcr = this.mcr();
			var effects = this.effects();

			console.log("Tosca: " + tosca);

			comot.deploy(this.tosca(), function(data) {
				console.log("ddddddddddddddddddddddddddddddddddddddddd");
				console.log(data);
			});

		};
	}

	return new AppViewModel();
});
