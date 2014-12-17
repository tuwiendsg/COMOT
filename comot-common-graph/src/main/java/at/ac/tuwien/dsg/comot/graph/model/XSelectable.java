package at.ac.tuwien.dsg.comot.graph.model;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public interface XSelectable {

	public Long getNodeId();

	public void setNodeId(Long nodeId);
}
