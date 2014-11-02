define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json-human');

	return {
		serviceId : ko.observable(),
		status : ko.observable(),
		checkStatus : function() {

			var that = this;

			$.ajax({
				type : "GET",
				url : "rest/service/" + that.serviceId(),
				dataType : "json",
				success : function(data) {

					that.status(data.state);

					$("#output").html(JsonHuman.format(data));
				}
			});

		}

	};
});
