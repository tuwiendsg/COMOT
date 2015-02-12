define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client');
	var repeater = require('repeater');

	var tab = repeater.create("Monitoring", 5000);

	var model = {
		startTab : function(serviceId) {
			tab.runWith(serviceId, function() {
				comot.monitoringData(serviceId, processStatus)
			})
		},
		stopTab : function() {
			tab.stop();
		},
		detached : function() {
			model.stopTab();
		},
	};

	return model;

	function processStatus(data) {

		$("#output_monitoring").html(JsonHuman.format(data));
		createTreeMonitoring(data, "#tree_monitoring");
	}
});

function createTreeMonitoring(root, divId) {

	// dimensions
	var margin = {
		top : 10,
		bottom : 10,
		left : 100
	};
	var boxWidth = 170;
	var boxHeightBase = 60;
	var boxHeightMax = boxHeightBase;

	var firstLineDY = -14;
	var lineSpaceDY = 18;
	var lineDX = -(boxWidth / 2) + 5;

	var height = 0; // computed in transformElement()
	var width = 1000;
	transformElement(root);

	if (height < 200) {
		height = 200;
	}

	var ratioWidth = 1050;
	var ratioHeight = height + margin.top + margin.bottom;

	var cluster = d3.layout.cluster().size([ height, width ]);
	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	$(divId).empty();
	var svg = d3.select(divId).append("svg").attr("viewBox", "0 0 " + ratioWidth + " " + ratioHeight).attr(
			"preserveAspectRatio", "xMidYMid").append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	var nodes = cluster.nodes(root);
	var visibleNodes = [];

	for (var i = 0; i < nodes.length; i++) {
		if (nodes[i].visible) {
			if (nodes[i].leaf) {
				nodes[i].children = undefined;
			}
			visibleNodes.push(nodes[i]);
		}
	}

	var links = cluster.links(visibleNodes);

	var link = svg.selectAll(".link").data(links).enter().append("path").attr("class", "link").attr("d", diagonal);
	var node = svg.selectAll(".node").data(visibleNodes).enter().append("g").attr("class", "node").attr("transform",
			function(d) {
				return "translate(" + d.y + "," + d.x + ")";
			});

	// set box -(boxHeight / 2)
	node.append("rect").attr("x", -(boxWidth / 2)).attr("y", function(d) {
		return -(d.height / 2);
	}).attr("width", boxWidth).attr("height", function(d) {
		return d.height;
	}).attr("rx", 7).attr("ry", 7);

	// set TYPE
	node.append("text").attr("dx", lineDX).attr("dy", function(d) {
		return computeLineSpaceDY(d.height, 0);
	}).attr("title", function(d) {
		return d.type;
	}).text(function(d) {
		return d.type;
	});
	// set NAME
	node.append("text").attr("class", "bold").attr("dx", lineDX).attr("dy", function(d) {
		return computeLineSpaceDY(d.height, 1);
	}).text(function(d) {
		return d.name;
	});
	// set METRICS
	for (var i = 0; i < 50; i++) {

		node.append("text").attr("dx", lineDX).attr("dy", function(d) {
			return computeLineSpaceDY(d.height, 2 + i);
		}).text(function(d) {

			var string = "";

			if (d.metrics.length > i) {
				var value;

				if (d.metrics[i].value.type === "double") {
					value = Number((d.metrics[i].value.value).toFixed(5));
				} else {
					value = d.metrics[i].value.value;
				}

				string = d.metrics[i].name + ": " + value + " " + d.metrics[i].measurementUnit;
			}
			return string;
		})
	}

	function computeLineSpaceDY(height, lineNr) {
		return -(height / 2) + (lineSpaceDY * lineNr) + 14;
	}

	function transformElement(element) { // traverse all elements recursively

		if (typeof element.children == 'undefined') {
			return;
		}

		// individual height of node
		var nodeHight = boxHeightBase + (element.metrics.length * lineSpaceDY)
		element.height = nodeHight;
		if (boxHeightMax < nodeHight) {
			boxHeightMax = nodeHight;
		}

		if (element.children.length === 0) {
			element.leaf = true;
			height = height + (nodeHight + 40)

			// set new children to make space
			element.children = [];

			var size = Math.ceil(element.metrics.length / 3);
			size = (size > 0) ? size : 1;

			for (var i = 0; i < size; i++) {
				element.children.push({
					name : "",
					visible : false
				});
			}
		} else {
			element.leaf = false;
		}

		// set visible
		element.visible = true;

		// set Name
		if (typeof element.instanceId === 'undefined') {
			element.name = element.id;
		} else {
			element.name = element.id + "_" + element.instanceId;
		}

		for (var i = 0; i < element.children.length; i++) {
			transformElement(element.children[i]);
		}
	}

}
