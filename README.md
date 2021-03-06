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

To get a first impression on what COMOT does for you, consider the following COMOT definition:


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

CloudApplication daasApplication = CloudApplication("DaaSApp")
    .withName("DaaS Cloud Application").consistsOfServices(daaSService)
    
```

The example above basically produces the exact same TOSCA definition from above using several conventions and shortcuts. Using COMOT over the plain TOSCA definition has two pretty obvious advantages. First, it's much shorter. And second - and even more important - you can understand what your cloud application will look like on first sight and are not required to understand the basic concepts and terminolgy inherent to TOSCA definitions.

## COMOT Terminology

todo: Describe `ServiceNode`, `OperatingSystemNode`, `ServiceTemplate` and `CloudApplication`

## Node Bundles

Making the definition of COMOT cloud applications even more concise and straightforward is the goal of _NodeBundle_s. A NodeBundle can be described as an extensible convention for recurring SoftwareNode requirements. What does that mean? Consider the Cassandra SoftwareNode definition from above:

```java
ServiceNode cassandraHeadNode = SingleSoftwareNode("CassandraHead")
    .withName("Cassandra head node (single instance)")
    .provides(Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP"))
    .deployedBy(SingleScriptArtifactTemplate("deployCassandraHead","http://void.org/salsa/deploy.sh"))
    .constrainedBy(LatencyConstraint("Co1").lessThan("0.5")
);
```

In this example, the COMOT client has to know the details of the underlying Cassandra deployment artifact. As those details are subject to change over time, COMOT provides a short-cut for defining a Cassandra node that leaves out the volatile details:

```java
CassandraNode cassandraNode = CassandraNode("CassandraHead")
    .withName("Cassandra head node (single instance)")
    .provides(Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP"))
    .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));
```

The example from above does not seem like a big saving on lines of code. But, besides the fact that you are not required to define the deployment details yourself, COMOT stores the details of the Cassandra node definition in a related bundle configuration that uses JSON or YAML to describe the detailed requirements of the Cassandra node:

```json
{
    "id": "cassandra",

    "deployment-config": {
        "uri": "http://134.158.75.65/artifacts/cassandra/deploy-cassandra-node.sh",
        "version": "2.0.7"
    },

    "runtime-config": {
        "environment": {
            "JAVA_OPTS": "-Xmx2g -Xms2g"
        },

        "arguments": "-Dcom.sun.management.jmxremote.port=18080",
        "logging-config": {
            "dir": "/var/log/cassandra"
        }
    }
}
```
   
Behind the scences, COMOT loads this bundle configuration and builds a `BundleConfig`. This BundleConfig is then passed to SALSA via various TOSCA property definitions. For overriding the defaults you either have the option to change the JSON definition in the bundle config or manipulate the BundleConfig object itself. 
                

## Using the SalsaClient

COMOT provides a simple to use client for interacting with SALSA. It allows to use SALSA services without knowing the details of the various REST services that are used for managing a cloud applications lifecycle, such as deploying and undeploying the application. 

The `SalsaClient` interface provides four basic methods:

* `deploy(cloudApplication)` to deploy a new `CloudApplication` to SALSA
* `undeploy(applicationId)` to undeploy a `CloudApplication` from SALSA using the ID assigned by SALSA during deployment
* `spawn(applicationId, topologyId, nodeId, instanceCount)` adding additional instances of a particular node
* `destroy(applicationId, topologyId, nodeId, instanceId)` removing a particular node instance

We can deploy the CloudApplication definition from above using the SalsaClient like this:

```java
SalsaClient client = new DefaultSalsaClient();
CloudApplication application = ... // build a cloud application following the sample from above
SalsaResponse response = client.deploy(application);
if (response.isExpected()) {
  // everything is fine
}
String applicationId = response.getMessage();
client.undeploy(applicationId); // ignoring response for brevity
```

A SalsaClient is configured using a `SalsaClientConfiguration` instance. If you are using a default SALSA installation, there is not much to configure. COMOT defaults to localhost:8080/salsa. In any case you can access the client's configuration using `client.getConfiguration()`, which allows for adapting to custom SALSA installations (e.g., using a different base URL)


## Build Server and Code Metrics
We are using Jenkins and Sonar for automatic builds and code metrics.

Our Jenkins installation is available [here](http://jenkins.infosys.tuwien.ac.at/)
For our Sonar installation, click [here](http://sonar.infosys.tuwien.ac.at/)


## More information

Coming soon. In the mean while check the [Wiki](https://github.com/tuwiendsg/COMOT/wiki) for more information.
