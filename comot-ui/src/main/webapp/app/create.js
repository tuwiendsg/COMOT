define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), comot = require('comot_client');

	function AppViewModel() {

		this.checkboxDepl = ko.observable();
		this.checkboxMoni = ko.observable();
		this.checkboxCont = ko.observable();

		this.tosca = ko.observable("");
		this.mcr = ko.observable();
		this.elEffects = ko.observable();

		this.deploy = function() {

			var desc = this.tosca();

			console.log("Tosca: " + desc);

			comot.deploy(this.tosca(), function(data) {
				console.log("ddddddddddddddddddddddddddddddddddddddddd");
				console.log(data);
			});

			// $.ajax({
			// type : "POST",
			// url : "rest/services",
			// data : this.tosca(),
			// dataType : "xml",
			// contentType : "application/xml",
			// success : function(data) {
			// console.log(data);
			// }
			// });

		};
	}

	return new AppViewModel();
});
