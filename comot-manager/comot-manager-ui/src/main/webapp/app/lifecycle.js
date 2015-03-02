define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), $ = require("jquery"), router = require('plugins/router');

	var notify = require('notify');

	var model = {

		lifecycle : {},
		detached : function() {
			console.log("detached closing EventSource");

			console.log(source);
			source.close();
			console.log(source);
		},
		deactivate : function() {
			console.log("deactivate closing EventSource");

			console.log(source);
			source.close();
			console.log(source);
		},
		attached : function() {

			comot.lifecycle(function(data) {

				lifecycle = data;

				createTree(lifecycle, "#tree_div", "xxx", "xxx");
			})

			registerForEvents(model);
		},
		doLifecycle : function(lastState, currentState) {
			createTree(lifecycle, "#tree_div", lastState, currentState);
		}
	}

	return model;

	function registerForEvents(model) {

		if (!!window.EventSource) {
			var source = new EventSource(comot.eventPath("aaa", "bbb"));

			source.addEventListener('message', function(e) {
				console.log(e);

				var listTrans = JSON.parse(e.data).List.list;

				$("#output").html(JsonHuman.format(listTrans));

				for (var i = 0; i < listTrans.length; i++) {
					if (listTrans[i].groupType === "SERVICE") {
						console.log("dostuff");
						model.doLifecycle(listTrans[i].currentState, listTrans[i].lastState);
						break;
					}
				}

			}, false);

			source.addEventListener('open', function(e) {
				console.log(e);

			}, false);

			source.addEventListener('error', function(e) {
				if (e.readyState == EventSource.CLOSED) {
					console.log("CLOSED");
					console.log(e);
					// Connection was closed.
				} else {
					console.log("SOMETHING");
					console.log(e);
				}
			}, false);

		} else {
			console.log("NO window.EventSource !!!")
		}
	}

});

function createTree(graph, divId, lastState, currentState) {

	$(divId).empty();
	
	var circleWidth = 15;
	var width = 960, height = 500;
	var force = d3.layout.force().charge(-1000).linkDistance(100).size([ width, height ]);
	var svg = d3.select(divId).append("svg").attr("width", width).attr("height", height);

	force.nodes(graph.nodes).links(graph.links).start();

	// Per-type markers, as they don't inherit styles.
	svg.append("defs").selectAll("marker").data([ "normal", "lastAction" ]).enter().append("marker").attr("id",
			function(d) {
				return d;
			}).attr("viewBox", "0 -5 10 10").attr("refX", 15).attr("refY", -1.5).attr("markerWidth", 6).attr(
			"markerHeight", 6).attr("orient", "auto").append("path").attr("d", "M0,-5L10,0L0,5");

	var link = svg.selectAll(".link").data(graph.links).enter().append("line").attr("class", function(d) {
		if (d.name === lastAction) {
			return "link lastAction";
		} else {
			return "link"
		}
	}).style("stroke-width", function(d) {
		return Math.sqrt(d.value);
	}).attr("marker-end", function(d) {
		// if (d.name === lastAction) {
		// return "url(#normal)";
		// } else {
		// return "url(#normal)";
		// }
		return "url(#normal)";
	});

	var node = svg.selectAll(".node").data(graph.nodes).enter().append("g").attr("class", "node").call(force.drag);

	node.append("title").text(function(d) {
		return d.name;
	});

	node.append("text").attr("dx", circleWidth + 3).attr("dy", 5).text(function(d) {
		return d.name
	});

	node.append("circle").attr("r", circleWidth).attr("class", function(d) {
		if (d.name === currentState) {
			return "lc current";
		} else if (d.name === lastState) {
			return "lc last";
		} else if (d.initFinal) {
			return "lc init";
		} else {
			return "lc";
		}
	}).call(force.drag);

	force.on("tick", function() {

		link.attr("x1", function(d) {
			return d.source.x;
		}).attr("y1", function(d) {
			return d.source.y;
		}).attr("x2", function(d) {
			return d.target.x;
		}).attr("y2", function(d) {
			return d.target.y;
		});

		node.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});

	});

}

function pokus(graph, divId) {

	var w = 960, h = 500
	markerWidth = 6, markerHeight = 6, cRadius = 30, // play with the cRadius value
	refX = cRadius + (markerWidth * 2), refY = -Math.sqrt(cRadius), drSub = cRadius + refY;

	var force = d3.layout.force().nodes(d3.values(graph.nodes)).links(graph.links).size([ w, h ]).linkDistance(150)
			.charge(-2000).on("tick", tick).start();

	var svg = d3.select(divId).append("svg:svg").attr("width", w).attr("height", h);

	// Per-type markers, as they don't inherit styles.
	svg.append("svg:defs").selectAll("marker").data([ "suit", "licensing", "resolved" ]).enter().append("svg:marker")
			.attr("id", String).attr("viewBox", "0 -5 10 10").attr("refX", refX).attr("refY", refY).attr("markerWidth",
					markerWidth).attr("markerHeight", markerHeight).attr("orient", "auto").append("svg:path").attr("d",
					"M0,-5L10,0L0,5");

	var path = svg.append("svg:g").selectAll("path").data(force.links()).enter().append("svg:path").attr("class",
			function(d) {
				return "link licensing";
			}).attr("marker-end", function(d) {
		return "url(#licensing)";
	});

	var circle = svg.append("svg:g").selectAll("circle").data(force.nodes()).enter().append("svg:circle").attr("r",
			cRadius).call(force.drag);

	var text = svg.append("svg:g").selectAll("g").data(force.nodes()).enter().append("svg:g");

	// A copy of the text with a thick white stroke for legibility.
	text.append("svg:text").attr("x", 0).attr("y", ".51em").attr("class", "shadow").text(function(d) {
		return d.name;
	});

	text.append("svg:text").attr("x", 0).attr("y", ".51em").text(function(d) {
		return d.name;
	});

	// Use elliptical arc path segments to doubly-encode directionality.
	function tick() {
		path.attr("d", function(d) {
			var dx = d.target.x - d.source.x, dy = (d.target.y - d.source.y), dr = Math.sqrt(dx * dx + dy * dy);
			return "M" + d.source.x + "," + d.source.y + "A" + (dr - drSub) + "," + (dr - drSub) + " 0 0,1 "
					+ d.target.x + "," + d.target.y;
		});

		circle.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});

		text.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
	}
}
