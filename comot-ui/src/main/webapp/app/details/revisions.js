define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client'), $ = require("jquery");
	var repeater = require('repeater');
	var notify = require('notify');

	var tab = repeater.create("Changes", 7000);
	var LONG_MAX = 9223372036854776000;
	var LONG_MAX_STRING = "9223372036854775807";

	var model = {
		selectedServiceId : ko.observable(""),
		selectedObjectId : ko.observable(""),
		selectedTime : ko.observable(LONG_MAX),
		startTab : function(serviceId) {
			tab.runWith(serviceId, function() {

				if (model.selectedServiceId() == "" || model.selectedServiceId() != serviceId) {
					model.selectedObjectId(serviceId);
					model.selectedTime(LONG_MAX);
				}

				model.selectedServiceId(serviceId);

				// refresh objects
				comot.getObjects(serviceId, function(data) {
					model.serviceObj.removeAll();
					model.topologiesObj.removeAll();
					model.unitsObj.removeAll();
					model.otherObj.removeAll();

					for (var i = 0; i < data.length; i++) {

						if (data[i].label == "CloudService") {
							model.serviceObj.push(data[i].id);
						} else if (data[i].label == "ServiceTopology") {
							model.topologiesObj.push(data[i].id);
						} else if (data[i].label == "ServiceUnit") {
							model.unitsObj.push(data[i].id);
						} else {
							model.otherObj.push(data[i].id);
						}

					}

					refreshChanges();

				}, function(error) {
					model.serviceObj.removeAll();
					model.topologiesObj.removeAll();
					model.unitsObj.removeAll();
					model.otherObj.removeAll();
					$("#output_revisions").html("");
					model.changes.removeAll();
					notify.info("No managed objects for service '" + serviceId + "'");
				});

			})
		},
		stopTab : function() {
			tab.stop();
		},
		detached : function() {
			model.stopTab();
		},
		serviceObj : ko.observableArray(),
		topologiesObj : ko.observableArray(),
		unitsObj : ko.observableArray(),
		otherObj : ko.observableArray(),

		changes : ko.observableArray(),
		revisionsForChange : function(timestamp) {
			model.selectedTime(timestamp);
			getRevision();
		},
		revisionsForObject : function(id) {
			model.selectedObjectId(id);
			getRevision();
		},
		attached : function() {

			$(document).ready(
					function() {

						$('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title',
								'Collapse this branch');
						$('.tree li.parent_li > span').on(
								'click',
								function(e) {
									var children = $(this).parent('li.parent_li').find(' > ul > li');
									if (children.is(":visible")) {
										children.hide('fast');
										$(this).attr('title', 'Expand this branch').find(' > i').addClass(
												'icon-plus-sign').removeClass('icon-minus-sign');
									} else {
										children.show('fast');
										$(this).attr('title', 'Collapse this branch').find(' > i').addClass(
												'icon-minus-sign').removeClass('icon-plus-sign');
									}
									e.stopPropagation();
								});
					});
		}

	};

	return model;

	function getRevision() {
		var timestamp;
		var serviceId = model.selectedServiceId();
		var objectId = model.selectedObjectId();

		if (model.selectedTime() == LONG_MAX) {
			timestamp = LONG_MAX_STRING;
		} else {
			timestamp = model.selectedTime() + 1;
		}

		comot.getRevision(serviceId, objectId, timestamp, function(data) {
			$("#output_revisions").html(JsonHuman.format(data));
		}, function(error) {
			$("#output_revisions").html("");
			notify.info("No revision for service '" + serviceId + "', object '" + objectId
					+ ((timestamp == LONG_MAX_STRING) ? " currently valid" : " ' at time '" + toDateString(timestamp))
					+ "'");
		});

		refreshChanges();
	}

	function refreshChanges() {

		var serviceId = model.selectedServiceId();
		var objectId = model.selectedObjectId();

		// refresh changes
		comot.getChanges(serviceId, objectId, function(data) {
			model.changes.removeAll();
			for (var i = 0; i < data.length; i++) {
				data[i].time = toDateString(data[i].timestamp);
				model.changes.push(data[i]);
			}
		}, function(error) {
			model.changes.removeAll();
			notify.info("No changes for service '" + serviceId + "', object '" + objectId + "'");
		})
	}

	function toDateString(long) {
		var date = new Date(long);
		var string = "" + date.getDate() + "." + date.getMonth() + 1 + "." + date.getFullYear() + " "
				+ (date.getHours() + 1) + ":" + date.getMinutes() + ":" + date.getSeconds();
		return string;
	}

});
