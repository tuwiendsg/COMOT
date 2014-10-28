
function AppViewModel() {

	var model = this;

	this.tosca = ko.observable();
	this.serviceId = ko.observable();
	this.status = ko.observable();

	this.deploy = function() {

		var desc = this.tosca();

		if (desc == "") {
			console.log("empty");
		}

		$.ajax({
			type : "POST",
			url : "http://localhost:8380/comot/rest/service",
			data : this.tosca(),
			contentType : "application/xml",
			success : function(data) {
				console.log(data);
				model.serviceId(data);
			}
		});
	};

	this.checkStatus = function() {

		$.ajax({
			type : "GET",
			url : "http://localhost:8380/comot/rest/service/"
					+ model.serviceId(),
			dataType : "json",
			success : function(data) {
				console.log(data);
				console.log(data.id);
				model.status(data.state);
			}
		});
	}

}
