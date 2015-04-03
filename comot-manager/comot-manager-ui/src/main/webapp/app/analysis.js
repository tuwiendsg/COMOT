define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), dimple = require('dimple'), JsonHuman = require('json_human'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var repeaterModule = require('repeater');
	var notify = require('notify');

	var LONG_MAX = 9223372036854776000;
	var LONG_MAX_STRING = "9223372036854775807";

	var UNIT = "UnitID";
	var INSTANCE = "InstanceID";
	var TYPE = "Type"
	var STAGE = "Stage";
	var LENGTH = "Length";
	var TIMESTAMP = "Timestamp";

	var orderStages = [ "ALLOCATING", "CONFIGURING", "STAGING", "INSTALLING" ];
	var graph1Div = "#output";
	var graph2Div = "#output2";
	var graph3Div = "#output3";

	var colors = [ new dimple.color("#c6dbef"), new dimple.color("#9ecae1"), new dimple.color("#6baed6"),
			new dimple.color("#3182bd"), new dimple.color("#e6550d") ];

	var allData;
	var allDataWithoutSum;

	var model = {
		// properties
		serviceId : ko.observable(""),
		instanceId : ko.observable(""),
		units : ko.observableArray(),
		instanceIds : ko.observableArray(),
		types : ko.observableArray(),
		stages : ko.observableArray(),
		// filter : ko.observable(UNIT),

		// functions
		switchUnit : function(unit) {
			if (unit.selected()) {
				unit.selected(false);
			} else {
				unit.selected(true);
			}
			for (var i = 0; i < model.types().length; i++) {
				model.types()[i].selected(false);
			}
			refreshGraphs(UNIT);
		},

		switchType : function(type) {
			if (type.selected()) {
				type.selected(false);
			} else {
				type.selected(true);
			}

			for (var i = 0; i < model.units().length; i++) {
				model.units()[i].selected(false);
			}
			refreshGraphs(TYPE);
		},

		// life-cycle
		activate : function(serviceId, instanceId) {

			model.serviceId(serviceId);
			model.instanceId(instanceId);

		},
		attached : function() {

			comot.getUnitInstanceDeploymentEvents(model.serviceId(), model.instanceId(), function(data) {

				console.log(data);
				allData = data;
				allDataWithoutSum = dimple.filterData(data, STAGE, orderStages);

				var unitIds = dimple.getUniqueValues(data, UNIT);
				var instanceIds = dimple.getUniqueValues(data, INSTANCE);
				var types = dimple.getUniqueValues(data, TYPE);
				var defValue;

				model.types.removeAll();
				for (var i = 0; i < types.length; i++) {

					if (i == 0) {
						defValue = true;
					} else {
						defValue = false;
					}

					model.types.push({
						'name' : types[i],
						'selected' : ko.observable(defValue)
					});
				}
				model.units.removeAll();
				for (var i = 0; i < unitIds.length; i++) {
					model.units.push({
						'name' : unitIds[i],
						'selected' : ko.observable(false)
					});
				}

				barChart(allDataWithoutSum);
				refreshGraphs(TYPE);

			});
		},
		detached : function() {

		}
	};

	function extractSelected(array) {
		var arrayNames = []

		for (var i = 0; i < array.length; i++) {
			if (array[i].selected()) {
				arrayNames[i] = array[i].name;
			}
		}
		return arrayNames;
	}

	function refreshGraphs(filterType) {

		var filteredData;

		if (filterType === UNIT) {
			filteredData = dimple.filterData(allDataWithoutSum, UNIT, extractSelected(model.units()));
		} else if (filterType === TYPE) {
			filteredData = dimple.filterData(allDataWithoutSum, TYPE, extractSelected(model.types()));
		}
		console.log("filteredData");
		console.log(filteredData);

		avgPieChart(filteredData);
		timeChart(filteredData);
	}

	function barChart(data) {
		$(graph2Div).empty();
		var svg = dimple.newSvg(graph2Div, "100%", 400);
		var chart = new dimple.chart(svg, data);
		chart.setBounds("17%", "5%", "78%", "60%");
		chart.addLegend(20, 20, 50, "100%", "left");
		//chart.defaultColors = colors;

		var x = chart.addCategoryAxis("x", UNIT);
		// x.addOrderRule("timestamp");

		var y = chart.addMeasureAxis("y", LENGTH);
		y.title = "Length in seconds";

		var series = chart.addSeries(STAGE, dimple.plot.bar);
		series.addOrderRule(orderStages);
		series.aggregate = dimple.aggregateMethod.avg

		chart.draw(500);
	}

	function avgPieChart(data) {

		$(graph1Div).empty();
		var svg = dimple.newSvg(graph1Div, "100%", 300);

		var chart = new dimple.chart(svg, data);
		chart.setBounds("15%", "15%", "80%", "70%");
		chart.addLegend(20, 20, 50, "100%", "left");
		//chart.defaultColors = colors;

		chart.addMeasureAxis("p", LENGTH);

		var series = chart.addSeries(STAGE, dimple.plot.pie);
		series.addOrderRule(orderStages);
		series.aggregate = dimple.aggregateMethod.avg

		chart.draw();
	}

	function timeChart(data) {

		$(graph3Div).empty();
		var svg = dimple.newSvg(graph3Div, "100%", 600);
		var chart = new dimple.chart(svg, data);
		chart.setBounds("17%", "5%", "78%", "50%");
		chart.addLegend(20, 20, 50, "100%", "left");
		//chart.defaultColors = colors;

		var x = chart.addCategoryAxis("x", [ INSTANCE, TIMESTAMP ]);
		x.addOrderRule(TIMESTAMP);

		var y = chart.addMeasureAxis("y", LENGTH);
		y.title = "Length in seconds";

		var s = chart.addSeries(STAGE, dimple.plot.line);
		s.addOrderRule(orderStages);

		var sSum = chart.addSeries("Total", dimple.plot.line);
		// s2.addOrderRule(orderStages);

		chart.draw(500);
	}

	return model;

});
