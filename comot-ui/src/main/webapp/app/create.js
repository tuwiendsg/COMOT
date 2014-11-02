define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http');

	function AppViewModel() {

		this.checkboxDepl = ko.observable();
		this.checkboxMoni = ko.observable();
		this.checkboxCont = ko.observable();

		this.tosca = ko.observable("");
		this.mcr = ko.observable();
		this.elEffects = ko.observable();

		this.deploy = function() {

			var desc = this.tosca();

			console.log("tosca: " + desc);

			var req = {
				tosca : this.tosca(),
				mcr : this.mcr(),
				elEffects : this.elEffects()
			};

			console.log(req);
			
			http.post("rest/service", req).then(function(response) {
				console.log("aa "+ data);
			});
/*
			$.ajax({
				type : "POST",
				url : "../rest/service",
				data : req,
				contentType : "application/json",
				success : function(data) {
					console.log(data);
				}
			});
			*/
		};
	}

	return new AppViewModel();
});
