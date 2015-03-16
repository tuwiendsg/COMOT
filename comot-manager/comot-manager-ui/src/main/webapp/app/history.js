define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var repeaterModule = require('repeater');
	var notify = require('notify');

	var repeater = repeaterModule.create("Changes", 10000);
	var LONG_MAX = 9223372036854776000;
	var LONG_MAX_STRING = "9223372036854775807";

	var model = {
		// properties
		serviceId : ko.observable(""),
		instanceId : ko.observable(""),
		selectedObjectId : ko.observable(""),
		selectedTime : ko.observable(LONG_MAX),
		serviceObj : ko.observableArray(),
		topologiesObj : ko.observableArray(),
		unitsObj : ko.observableArray(),
		otherObj : ko.observableArray(),
		changes : ko.observableArray(),
		// functions
		revisionsForChange : function(timestamp) {
			model.selectedTime(timestamp);
			getRevision();
		},
		revisionsForObject : function(id) {
			model.selectedObjectId(id);
			getRevision();
		},
		// life-cycle
		activate : function(serviceId, instanceId) {

			model.serviceId(serviceId);
			model.instanceId(instanceId);

		},
		attached : function() {

			showThisInstance(model.serviceId(), model.instanceId());

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
		},
		detached : function() {
			repeater.stop();
		}
	};

	return model;

	function showThisInstance(serviceId, instanceId) {

		repeater.runWith(instanceId, function() {

			if (model.selectedObjectId() == "" || model.instanceId() != instanceId) {
				model.selectedObjectId(model.serviceId());
				model.selectedTime(LONG_MAX);
			}

			model.serviceId(serviceId);
			model.instanceId(instanceId);

			// refresh objects
			comot.getObjects(instanceId, function(data) {
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
				notify.info("No managed objects for service '" + instanceId + "'");
			});

		})
	}

	function getRevision() {
		var timestamp;
		var instanceId = model.instanceId();
		var objectId = model.selectedObjectId();

		if (model.selectedTime() == LONG_MAX) {
			timestamp = LONG_MAX_STRING;
		} else {
			timestamp = model.selectedTime() + 1;
		}

		comot.getRecording(instanceId, objectId, timestamp, function(data) {
			$("#output_revisions").html(JsonHuman.format(data));
		}, function(error) {
			$("#output_revisions").html("");
			notify.info("No revision for service '" + instanceId + "', object '" + objectId
					+ ((timestamp == LONG_MAX_STRING) ? " currently valid" : " ' at time '" + toDateString(timestamp))
					+ "'");
		});

		refreshChanges();
	}

	function refreshChanges() {

		var instanceId = model.instanceId();
		var objectId = model.selectedObjectId();

		// refresh changes
		comot.getAllEvents(instanceId, function(data) {
			model.changes.removeAll();
			for (var i = 0; i < data.length; i++) {

				var propsArr = data[i].propertiesMap.entry;
				var props = {};
				for (var j = 0; j < propsArr.length; j++) {
					props[propsArr[j].key] = propsArr[j].value;
				}

				data[i].props = props;
				data[i].time = toDateString(data[i].timestamp);

				model.changes.push(data[i]);
			}
		}, function(error) {
			model.changes.removeAll();
			notify.info("No changes for service '" + instanceId + "', object '" + objectId + "'");
		})
	}

	function toDateString(long) {
		var date = new Date(long);
		var string = "" + date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear() + " "
				+ (date.getHours() + 1) + ":" + date.getMinutes() + ":" + date.getSeconds();
		return string;
	}

});
