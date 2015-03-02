define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var notify = require('notify');
	var lifecycle = {};
	var source = {};

	var model = {
		// properties
		serviceId : ko.observable(""),
		instanceId : ko.observable(""),
		state : ko.observable(),
		events : ko.observableArray(),
		allEpsServices : ko.observableArray(),
		selectedEpsServices : ko.observableArray(),
		allServices : ko.observableArray(),
		// functions
		startInstance : startInstance,
		stopInstance : stopInstance,
		assignEps : assignEps,
		removeEps : removeEps,
		showThisInstance : showThisInstance,
		// life-cycle
		deactivate : function() {
			if (typeof source.close === 'function') {
				source.close();
			}
		},
		activate : function(input) {

			if (typeof input != 'undefined') {
				model.serviceId(input.serviceId);
				model.instanceId(input.instanceId);
			}

			comot.getEps(function(data) {
				model.allEpsServices(data);
			});
			comot.getAllInstances(function(data) {
				model.allServices(data);
			});
		},
		attached : function() {

			comot.lifecycle(function(data) {
				lifecycle = data;

				if (model.serviceId() != "" && model.instanceId() != "") {
					showThisInstance(model.serviceId(), model.instanceId());
				} else {
					createLifecycle(lifecycle, "#lifecycle_div", "xxx", "xxx");
				}
			});

		}
	}

	return model;

	function showThisInstance(serviceId, instanceId) {

		console.log("showing: " + serviceId + " " + instanceId);

		if (typeof source.close === 'function') {
			source.close();
		}

		model.events.removeAll();
		model.selectedEpsServices.removeAll();

		model.serviceId(serviceId);
		model.instanceId(instanceId);

		comot.getServiceInstance(serviceId, instanceId, function(data) {

			var transitions = data.transitions.entry;
			var service = data.service;
			var epses = service.ServiceInstances.Instance[0].support;
	
			for (var i = 0; i < epses.length; i++) {
				model.selectedEpsServices.push(epses[i]);
			}
			
			populateGraphs(lifecycle, service, transitions);
		});

		var path = comot.eventPath(serviceId, instanceId);
		source = registerForEvents(path, lifecycle, model.events);

	}

	function startInstance() {
		console.log("start");

		if (model.serviceId() === "" || model.instanceId() === "") {
			return;
		}

		comot.startServiceInstance(model.serviceId(), model.instanceId(), function(data) {

		});
	}

	function stopInstance(serviceId, instanceId) {
		console.log("stop");
		
		if (model.serviceId() === "" || model.instanceId() === "") {
			return;
		}

		comot.stopServiceInstance(model.serviceId(), model.instanceId(), function(data) {

		});
		
		
	}

	function assignEps(eps) {
		console.log("eps");

		comot.assignSupportingEps(model.serviceId(), model.instanceId(), eps.id, function(data) {

			model.selectedEpsServices.push(eps);
		});
	}
	
	function removeEps(eps) {
		console.log("eps");
		var epsId = eps.id;

		comot.removeSupportingEps(model.serviceId(), model.instanceId(), epsId, function(data) {

			model.selectedEpsServices.remove(function(item) { return item.id === epsId });
		});
	}

});

function registerForEvents(path, lifecycle, events) {

	if (!!window.EventSource) {
		var source = new EventSource(path);

		source.addEventListener('message', function(e) {
			// console.log(e);
			var message = JSON.parse(e.data);
			var transitions = message.stateMessage.transitions.entry;
			var event = message.stateMessage.event;
			var service = event.service;

			// events
			showEvent(events, event);

			if (typeof event.action !== 'undefined') {
				populateGraphs(lifecycle, service, transitions);
			}

		}, false);

		source.addEventListener('error', function(e) {
			console.log(e);
		}, false);

	} else {
		console.log("NO window.EventSource !!!")
	}

	return source;
}

function populateGraphs(lifecycle, service, transitions) {

	var serviceId = service.id;
	var tMap = [];
	for (i = 0; i < transitions.length; i++) {
		tMap[transitions[i].value.groupId] = transitions[i].value;
	}
	console.log("aaaaaaaaaa")
	console.log(service)
	console.log(tMap)

	// lifecycle
	createLifecycle(lifecycle, "#lifecycle_div", tMap[serviceId].lastState, tMap[serviceId].currentState);

	console.log("bbbbbbbbbbb")
	console.log(service)
	console.log(tMap)

	// tree
	createTree(createElement(service, tMap), "#tree_div");

	// human json
	// $("#output").html(JsonHuman.format(transitions));
}

function showEvent(events, event) {

	var name;
	if (typeof event.action === 'undefined') {
		name = event.customEvent;
	} else {
		name = event.action;
	}

	events.push({
		'name' : name,
		'target' : event.groupId
	});

}

function createElement(object, tMap) {

	var type = tMap[object.id].groupType;
	var members;

	var element = {
		'name' : object.id,
		'children' : [],
		'state' : tMap[object.id].currentState,
		'type' : type
	};

	if (type === "TOPOLOGY" || type === "SERVICE") {
		members = object.Topology;
		if (typeof members != 'undefined') {
			for (var i = 0; i < members.length; i++) {
				element.children.push(createElement(members[i], tMap));
			}
		}
	}

	if (type === "TOPOLOGY") {
		members = object.ServiceUnits.Unit;
		if (typeof members != 'undefined') {
			for (var i = 0; i < members.length; i++) {
				element.children.push(createElement(members[i], tMap));
			}
		}

	} else if (type === "UNIT") {
		members = object.Instances.instances;
		if (typeof members != 'undefined') {
			for (var i = 0; i < members.length; i++) {
				element.children.push(createElement(members[i], tMap));
			}
		}
	}

	return element;
}

function createLifecycle(graph, divId, lastState, currentState) {

	console.log(graph);
	console.log(lastState);
	console.log(currentState);

	$(divId).empty();

	var circleWidth = 12;
	var width = 700, height = 500;
	var force = d3.layout.force().charge(-1000).linkDistance(100).size([ width, height ]);
	var svg = d3.select(divId).append("svg").attr("viewBox", "0 0 " + width + " " + height).attr("preserveAspectRatio",
			"xMidYMid").append("g").attr("transform", "translate(0,0)");

	force.nodes(graph.nodes).links(graph.links);

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
			return Math.max(circleWidth, Math.min(width - circleWidth, d.source.x));
		}).attr("y1", function(d) {
			return Math.max(circleWidth, Math.min(height - circleWidth, d.source.y));
		}).attr("x2", function(d) {
			return Math.max(circleWidth, Math.min(width - circleWidth, d.target.x));
		}).attr("y2", function(d) {
			return Math.max(circleWidth, Math.min(height - circleWidth, d.target.y));
		});

		node.attr("transform", function(d) {
			return "translate(" + Math.max(circleWidth, Math.min(width - circleWidth, d.x)) + ","
					+ Math.max(circleWidth, Math.min(height - circleWidth, d.y)) + ")";
		});

	});

	force.start();

}

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
				return d.type;
			}).text(function(d) {
				return d.type;
			});
	node.append("text").attr("class", "bold").attr("dx", lineDX).attr("dy", firstLineDY + (lineSpaceDY * 1)).text(
			function(d) {
				return d.name;
			});
	node.append("text").attr("class", function(d) {

		if (d.state === "DEPLOYED") {
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

		for (var i = 0; i < element.children.length; i++) {
			transformElement(element.children[i]);
		}
	}

}
