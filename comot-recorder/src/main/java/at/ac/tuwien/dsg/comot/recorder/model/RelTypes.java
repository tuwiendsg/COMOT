package at.ac.tuwien.dsg.comot.recorder.model;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
	_HAS_STATE, _MANAGE, _FIRST_REV, _LAST_REV
}
