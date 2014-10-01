package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;

@NodeEntity
public class PrimitiveOperation {
	@GraphId Long id;
	String name;
	// for performing the operation
	ExecutionType executionType = ExecutionType.SCRIPT;
	String executionREF = "/bin/date";
	String executionParameter = "";
	String executionOutput = "";
	
	
		
	public enum ExecutionType{
		SCRIPT, RESTful, SALSA_CONNECTOR;
	}
	
	public PrimitiveOperation() {}
	
	public static PrimitiveOperation newCommandType(String name, String executionCommand){		
		PrimitiveOperation po = new PrimitiveOperation();
		po.executionREF = executionCommand;
		po.executionType = ExecutionType.SCRIPT;
		po.name = name;
		return po;
	}
	
	public static PrimitiveOperation newScriptFromURLType(String name, String scriptRelativePath){
		PrimitiveOperation po = new PrimitiveOperation();		
		po.executionType = ExecutionType.SCRIPT;
		po.executionREF = scriptRelativePath;		
		po.name = name;
		return po;
	}
	
	public static PrimitiveOperation newSalsaConnector(String name, String connectorName){
		PrimitiveOperation po = new PrimitiveOperation();		
		po.executionType = ExecutionType.SALSA_CONNECTOR;
		po.executionREF = connectorName;
		po.name = name;
		return po;
	}

	public ExecutionType getExecutionType() {
		return executionType;
	}

	public void setExecutionType_(ExecutionType executionType) {
		this.executionType = executionType;
	}

	public String getExecutionREF() {
		return executionREF;
	}

	public void setExecutionREF_(String executionREF) {
		this.executionREF = executionREF;
	}

	public String getExecutionParameter() {
		return executionParameter;
	}

	public void setExecutionParameter_(String executionParameter) {
		this.executionParameter = executionParameter;
	}
	
	
}
