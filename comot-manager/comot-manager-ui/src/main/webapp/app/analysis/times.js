/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {

			comot.getUnitInstanceDeploymentEvents(model.serviceId(), function(data) {

				// console.log(data);
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
		var svg = dimple.newSvg("#output3temp", "100%", 600);
		var chart = new dimple.chart(svg, data);
		chart.setBounds("17%", "5%", "78%", "50%");
		chart.addLegend(20, 20, 50, "100%", "left");
		// chart.defaultColors = colors;

		var x = chart.addCategoryAxis("x", [ INSTANCE, TIMESTAMP ]);
		x.addOrderRule(TIMESTAMP);

		var y = chart.addMeasureAxis("y", LENGTH);
		y.title = "Length in seconds";

		var s = chart.addSeries(STAGE, dimple.plot.line);
		s.addOrderRule(orderStages);

		var sSum = chart.addSeries("Total", dimple.plot.line);
		// s2.addOrderRule(orderStages);

		chart.draw(500);
		$("#output3temp").appendTo("#output3");
	}

	return model;

});
