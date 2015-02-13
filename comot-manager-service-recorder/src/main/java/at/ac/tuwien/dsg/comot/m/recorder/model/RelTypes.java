package at.ac.tuwien.dsg.comot.m.recorder.model;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
	_HAS_STATE, _MANAGE, _FIRST_REV, _LAST_REV
}
