# Introduction

COMOT triprovides several integration features for SALSA, MELA and rSYBL.

Cloud applications can be specified via TOSCA, and TOSCA specifications can be generated via a DSL like
fluent Java API. Consider this:

### A first example

```java
ServiceNode cassandraHeadNode = SingleSoftwareNode("CassandraHead")
                .withName("Cassandra head node (single instance)")

                .provides(
                        Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP")
                )

                .deployedBy(
                        SingleScriptArtifactTemplate(
                                "deployCassandraHead",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraHead.sh"
                        )
                )

                .constrainedBy(
                        LatencyConstraint("Co1").should(LessThan).value("0.5")
                );
```


## More information
