# What is COMOT?

COMOT provides an easy to use integration layer for managing various aspects of Cloud applications. In particular, COMOT relies on [SALSA](https://github.com/tuwiendsg/SALSA) for bootstrapping Cloud applications, [MELA](https://github.com/tuwiendsg/MELA) for Cloud application monitoring as well as [rSYBL](https://github.com/tuwiendsg/SALSA) for analyzing Cloud application behaviour and adaptation.

SALSA, MELA and rSYBL use [TOSCA](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=tosca) - an OASIS approved standard for describing the topology and orchestration of Cloud applications - for defining Cloud application. As an example, consider the folling TOSCA definition fragment:

```xml
<tosca:ServiceTemplate id="DaaSService">
    <tosca:BoundaryDefinitions>
        <tosca:Policies>
            <tosca:Policy name="CG0: CONSTRAINT Cost &lt; 1000 $"
                          policyType="SYBLConstraint"/>
        </tosca:Policies>
    </tosca:BoundaryDefinitions>
    <tosca:TopologyTemplate>
        <tosca:NodeTemplate id="CassandraHead"
                            name="Cassandra head node" type="software"
                            minInstances="1" maxInstances="1">
            <tosca:Capabilities>
                <tosca:Capability name="Data controller IP" type="variable"
                                  id="CassandraHeadIP_capa"/>
            </tosca:Capabilities>
            <tosca:Policies>
			    <tosca:Policy name="Co1:CONSTRAINT latency &lt; 0.5 ms;Co2:CONSTRAINT cpuUsage &lt; 83 %"
					          policyType="SYBLConstraint" />
					
	        </tosca:Policies>
            <tosca:DeploymentArtifacts>
                <tosca:DeploymentArtifact artifactType="tosca:script" name="Deployment script"
                                          artifactRef="deployCassandraHead"/>
            </tosca:DeploymentArtifacts>
        </tosca:NodeTemplate>
        <tosca:NodeTemplate type="os" id="OS_HeadNode">
            <tosca:Properties>
                <MappingProperties>
                    <MappingProperty type="os">
                        <property name="instanceType">m1.small</property>
                        <property name="provider">dsg@openstack</property>
                        <property name="baseImage">ami-00000163</property>
                        <property name="packages">openjdk-7-jre</property>
                    </MappingProperty>
                </MappingProperties>
            </tosca:Properties>
        </tosca:NodeTemplate>
        <tosca:RelationshipTemplate name="Relation" type="HOSTON" id="data2os">
            <tosca:SourceElement ref="CassandraHead"/>
            <tosca:TargetElement ref="OS_HeadNode"/>
        </tosca:RelationshipTemplate>
    </tosca:TopologyTemplate>
</tosca:ServiceTemplate>

<tosca:ArtifactTemplate id="deployCassandraHead" type="tosca:script">
    <tosca:ArtifactReferences>
        <tosca:ArtifactReference reference="http://void.org/salsa/deploy.sh"/>
    </tosca:ArtifactReferences>
</tosca:ArtifactTemplate>
```
        
In short, the example above specifies a cloud application that consists of a single Cassandra node hosted on an OpenStack provided VM. It's easy to see that even such a simple cloud application requires in-depth knowledge of TOSCA as well as SALSA specific extensions for TOSCA (e.g., the MappingProperties from above).

COMOT tries to help users with the specification of Cloud applications by providing a DSL like, fluent Java API that creates a TOSCA specifiction from very little input using defaults and conventions.  

## A first example



```java
ServiceNode cassandraHeadNode = SingleSoftwareNode("CassandraHead")
    .withName("Cassandra head node (single instance)")
    .provides(Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP"))
    .deployedBy(SingleScriptArtifactTemplate("deployCassandraHead","http://void.org/salsa/deploy.sh"))
    .constrainedBy(LatencyConstraint("Co1").lessThan("0.5")
);
    
OperatingSystemNode cassandraHeadOsNode = OperatingSystemNode("OS_Headnode")
    .providedBy(OpenstackSmall("OS_Headnode_Small")
        .withProvider("dsg@openstack")
        .addSoftwarePackage("openjdk-7-jre")  
);    
    
ServiceTemplate daaSService = ServiceTemplate("DaasService")
    .constrainedBy(CostConstraint("CG0").lessThan("1000"))
    .definedBy(ServiceTopology("DaasTopology")
        .consistsOfNodes(cassandraHeadNode,cassandraHeadOsNode)
        .andRelationships(HostedOnRelation("data2os").from(cassandraHeadNode).to(cassandraHeadOsNode)
    )
);
    
```

## Build Server and Code Metrics
We are using Jenkins and Sonar for automatic builds and code metrics.

Our Jenkins installation is available [here](http://jenkins.infosys.tuwien.ac.at/)
For our Sonar installation, click [here](http://sonar.infosys.tuwien.ac.at/)


## More information

Coming soon. In the mean while check the [Wiki](https://github.com/tuwiendsg/COMOT) for more information.
