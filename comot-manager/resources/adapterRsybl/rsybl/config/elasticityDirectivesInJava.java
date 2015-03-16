@SYBL_ServiceTopologyDirective(annotatedEntityID="CassandraServiceTopology",
	    		 					constraints="Co1:CONSTRAINT latency.average < 30 ms;"+ 
									             "Co2:CONSTRAINT cpu.usage < 80 %")

@SYBL_ServiceTopologyDirective(annotatedEntityID="WebServiceTopology",
	    		 					constraints="Co3:CONSTRAINT responseTime < 450 ms;")

@SYBL_ServiceUnitDirective(annotatedEntityID="WebService",
	    		 					strategies="St1:STRATEGY CASE responseTime < 360 ms AND throughput_average<300 : scalein")									

@SYBL_CloudServiceDirective(annotatedEntityID="CloudService",
	    		 					constraints="Co4:CONSTRAINT cost.PerHour < 60 Euro;"+
									"Co5:CONSTRAINT costPerClientPerHour < 3 Euro")
																		
									
									