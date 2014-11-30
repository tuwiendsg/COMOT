define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), JsonHuman = require('json_human'), d3 = require('d3'), comot = require('comot_client');
	var notify = require('notify');
	var repeater = require('repeater');

	var tab = repeater.create("Structure", 7000);

	var model = {
		startTab : function(serviceId) {
			tab.runWith(serviceId, function() {
				comot.checkStatus(serviceId, processStatus)
			})
		},
		stopTab : function() {
			tab.stop();
		},
		detached : function() {
			model.stopTab();
		},
		status : ko.observable()
	};

	return model;

	function processStatus(data) {

		model.status(data.state);

		$("#output").html(JsonHuman.format(data));
		createTree(data, "#tree_div");
	}
});

function createTree(root, divId) {

	// dimensions
	var margin = {
		top : 10,
		right : 10,
		bottom : 10,
		left : 100
	};
	var boxWidth = 170;
	var boxHeight = 60;

	var width = 1050;

	var firstLineDY = -14;
	var lineSpaceDY = 18;
	var lineDX = -(boxWidth / 2) + 5;

	// compute
	var countLeafs = 0;
	transformElement(root);

	var heightCore = countLeafs * (boxHeight + 30);
	if (heightCore < 200) {
		heightCore = 200;
	}
	var height = heightCore + margin.top + margin.bottom;

	var cluster = d3.layout.cluster().size([ heightCore, 700 ]);
	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	$(divId).empty();
	var svg = d3.select(divId).append("svg").attr("viewBox", "0 0 " + width + " " + height).attr("preserveAspectRatio",
			"xMidYMid").append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	var nodes = cluster.nodes(root);
	var links = cluster.links(nodes);

	var link = svg.selectAll(".link").data(links).enter().append("path").attr("class", "link").attr("d", diagonal);

	// add CONNECT_TO relationships
	nodes.forEach(function(d1) {
		nodes.forEach(function(d2) {

			for (var i = 0; i < d1.connectToIds.length; i++) {
				if (d2.id == d1.connectToIds[i]) {
					// console.log("Connect the " + d1.id + " and " + d2.id);

					links.push({
						"source" : d1,
						"target" : d2,
						"connectto" : true
					});
				}
			}
		});
	});

	// var link = svg.selectAll(".link").data(links).enter().append("path").attr("class", "link").attr("d",
	// function(d) {
	// return arc(d);
	// }).attr("transform",
	// function(d) {
	// return "translate(" + d.source.y + "," + d.source.x + ")";
	// });
	var node = svg.selectAll(".node").data(nodes).enter().append("g").attr("class", "node").attr("transform",
			function(d) {
				return "translate(" + d.y + "," + d.x + ")";
			});

	// set box
	node.append("rect").attr("x", -(boxWidth / 2)).attr("y", -(boxHeight / 2)).attr("width", boxWidth).attr("height",
			boxHeight).attr("rx", 7).attr("ry", 7);

	// set lines
	node.append("text").attr("dx", lineDX).attr("dy", firstLineDY + (lineSpaceDY * 0)).attr("data-toggle", "tooltip")
			.attr("title", function(d) {
				return d.type + " aaaa";
			}).text(function(d) {
				return d.type;
			});
	node.append("text").attr("class", "bold").attr("dx", lineDX).attr("dy", firstLineDY + (lineSpaceDY * 1)).text(
			function(d) {
				return d.name;
			});
	node.append("text").attr("class", function(d) {

		if (d.state === "RUNNING" || d.state === "DEPLOYED") {
			return "bold svg_ok";
		} else if (d.state === "ERROR") {
			return "bold svg_error";
		} else {
			return "bold svg_warn";
		}
	}).attr("dx", lineDX).attr("dy", firstLineDY + (lineSpaceDY * 2)).text(function(d) {
		return d.state;
	})

	function transformElement(element) { // traverse all elements recursively

		if (element.children.length === 0) {
			countLeafs = countLeafs + 1;
		}

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
