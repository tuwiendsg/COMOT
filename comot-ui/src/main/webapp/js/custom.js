function AppViewModel() {

	var model = this;

	this.tosca = ko.observable();
	this.serviceId = ko.observable();
	this.status = ko.observable();
	this.statusTree = ko.observable();

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

				checkStatus();
				setInterval(function() {
					checkStatus()
				}, 5000);
			}
		});
	};

	this.checkStatus = function() {
		checkStatus()
	}

	function checkStatus() {

		$.ajax({
			type : "GET",
			url : "http://localhost:8380/comot/rest/service/" + model.serviceId(),
			dataType : "json",
			success : function(data) {

				model.status(data.state);

				$("#output").html(JsonHuman.format(data));
			}
		});

	}

}
