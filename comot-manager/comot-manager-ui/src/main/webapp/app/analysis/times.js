/***********************************************************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 * 
 * This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8
 * \#317790)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
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

	var colors = [ new dimple.color("#c6dbef"), new dimple.color("#9ecae1"), new dimple.color("#6baed6"),
			new dimple.color("#3182bd"), new dimple.color("#e6550d") ];

	var allData;
	var allDataWithoutSum;

	var model = {
		// properties
		serviceId : ko.observable(""),
		units : ko.observableArray(),
		instanceIds : ko.observableArray(),
		types : ko.observableArray(),
		stages : ko.observableArray(),
		allUnits : ko.observableArray(),
		aggregatedUnits : ko.observableArray(),

		// functions
		switchUnit : function(unit) {

			for (var i = 0; i < model.types().length; i++) {
				model.types()[i].selected(false);
			}
			for (var i = 0; i < model.units().length; i++) {
				model.units()[i].selected(false);
			}

			unit.selected(true);

			refreshGraphs(UNIT);
		},

		switchType : function(type) {

			for (var i = 0; i < model.types().length; i++) {
				model.types()[i].selected(false);
			}
			for (var i = 0; i < model.units().length; i++) {
				model.units()[i].selected(false);
			}

			type.selected(true);

			refreshGraphs(TYPE);
		},

		// life-cycle
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {

			comot.getUnitInstanceDeploymentEvents(model.serviceId(), function(data) {

				// console.log(data);

				for (var i = 0; i < data.length; i++) {
					data[i].formatedTime = utils.longToDateStringMachine(data[i].Timestamp);
					console.log(data[i]);
				}

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

				barChart();
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

		avgPieChart(filteredData);
		timeChart(filteredData);

		if (filterType === UNIT) {
			filteredData = dimple.filterData(allData, UNIT, extractSelected(model.units()));
		} else if (filterType === TYPE) {
			filteredData = dimple.filterData(allData, TYPE, extractSelected(model.types()));
		}

		model.aggregatedUnits.removeAll();
		model.aggregatedUnits(avgsForUnit(filteredData));

	}

	function avgsForUnit(data) {

		var unitIds = dimple.getUniqueValues(data, UNIT);

		var resultArray = [];
		var allInstancesCount = 0;
		var allTotalTime = 0;

		for (var i = 0; i < unitIds.length; i++) {
			var instancesCount = 0;
			var totalTime = 0;

			for (var j = 0; j < data.length; j++) {
				if (data[j].UnitID === unitIds[i] && data[j].Stage === "SUM") {
					instancesCount = instancesCount + 1;
					totalTime = totalTime + data[j].Length;
					allInstancesCount = allInstancesCount + 1;
					allTotalTime = allTotalTime + data[j].Length;
				}
			}
			var avgTime = totalTime / instancesCount;

			resultArray.push({
				'name' : unitIds[i],
				'instancesCount' : instancesCount,
				'avgTime' : Math.round(avgTime)
			});
		}

		resultArray.push({
			'name' : "ALL",
			'instancesCount' : allInstancesCount,
			'avgTime' : Math.round(allTotalTime / allInstancesCount)
		});

		return resultArray;
	}

	function barChart() {

		model.allUnits.removeAll();
		model.allUnits(avgsForUnit(allData));
		model.allUnits.sort(function(left, right) {
			return left.avgTime == right.avgTime ? 0 : (left.avgTime < right.avgTime ? 1 : -1)
		})

		$("#output1temp").empty();
		var svg = dimple.newSvg("#output1temp", "100%", 300);
		// POSTER var svg = dimple.newSvg(graph2Div, "100%", 400);
		var chart = new dimple.chart(svg, allDataWithoutSum);
		// POSTER chart.setBounds("20%", "5%", "78%", "60%");
		chart.setBounds("22%", "5%", "73%", "80%");
		chart.addLegend(10, 20, 50, "100%", "left");
		// chart.defaultColors = colors;

		var x = chart.addCategoryAxis("x", UNIT);
		x.addOrderRule(LENGTH);

		var y = chart.addMeasureAxis("y", LENGTH);
		y.title = "Length in seconds";

		var series = chart.addSeries(STAGE, dimple.plot.bar);
		series.addOrderRule(orderStages);
		series.aggregate = dimple.aggregateMethod.avg

		chart.draw(500);
		$("#output1temp").appendTo("#output1");
	}

	function avgPieChart(data) {

		$("#output2temp").empty();
		var svg = dimple.newSvg("#output2temp", "100%", 200);

		var chart = new dimple.chart(svg, data);
		chart.setBounds("15%", "15%", "80%", "80%");
		chart.addLegend(20, 20, 50, "100%", "left");
		// chart.defaultColors = colors;

		chart.addMeasureAxis("p", LENGTH);

		var series = chart.addSeries(STAGE, dimple.plot.pie);
		series.addOrderRule(orderStages);
		series.aggregate = dimple.aggregateMethod.avg

		chart.draw();
		$("#output2temp").appendTo("#output2");
	}

	function timeChart(data) {

		$("#output3temp").empty();
		var svg = dimple.newSvg("#output3temp", "100%", 350);
		var chart = new dimple.chart(svg, data);
		chart.setBounds("17%", "5%", "78%", "80%");
		chart.addLegend(20, 20, 50, "100%", "left");
		// chart.defaultColors = colors;

		// 30.5.2015 18:0:43

		var x = chart.addTimeAxis("x", "formatedTime", "%d %m %Y %H %M %S %L", "%d.%m.%Y %H:%M");
		x.title = "Time";
		// x.timePeriod = d3.time.hours;
		// x.timeInterval = 1;
		x.ticks = 10;

		// var x = chart.addCategoryAxis("x", [ TIMESTAMP, INSTANCE ]);
		// x.addOrderRule(TIMESTAMP);
		// x.tickFormat = "%x %X";

		var y = chart.addMeasureAxis("y", LENGTH);
		y.title = "Length in seconds";

		var s = chart.addSeries(STAGE, dimple.plot.line);
		s.addOrderRule(orderStages);
		s.lineMarkers = true;

		// s.addEventHandler("mouseover", onHover);
		// s.addEventHandler("mouseleave", onLeave);

		var sSum = chart.addSeries("Total", dimple.plot.line);
		// sSum.addOrderRule(orderStages);
		sSum.lineMarkers = true;

		chart.draw(500);
		$("#output3temp").appendTo("#output3");
	}

	return model;

	// Event to handle mouse enter
	function onHover(e) {

		console.log(e);

		// Get the properties of the selected shape
		var cx = parseFloat(e.selectedShape.attr("x")), cy = parseFloat(e.selectedShape.attr("y"));

		// Set the size and position of the popup
		var width = 150, height = 70, x = (cx + width + 10 < svg.attr("width") ? cx + 10 : cx - width - 20);
		y = (cy - height / 2 < 0 ? 15 : cy - height / 2);

		// Create a group for the popup objects
		popup = svg.append("g");

		// Add a rectangle surrounding the text
		popup.append("rect").attr("x", x + 5).attr("y", y - 5).attr("width", 150).attr("height", height).attr("rx", 5)
				.attr("ry", 5).style("fill", 'white').style("stroke", 'black').style("stroke-width", 2);

		// Add multiple lines of text
		popup.append('text').attr('x', x + 10).attr('y', y + 10).append('tspan').attr('x', x + 10).attr('y', y + 20)
				.text('Species: ' + e.seriesValue[0]).style("font-family", "sans-serif").style("font-size", 10).append(
						'tspan').attr('x', x + 10).attr('y', y + 40).text("aaaaa " + e.InstanceID).style("font-family",
						"sans-serif").style("font-size", 10)
	}

	// Event to handle mouse exit
	function onLeave(e) {
		// Remove the popup
		if (popup !== null) {
			popup.remove();
		}
	}
	;

});
