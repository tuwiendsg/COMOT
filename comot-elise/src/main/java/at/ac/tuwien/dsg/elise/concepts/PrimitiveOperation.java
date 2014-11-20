package at.ac.tuwien.dsg.elise.concepts;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

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
		po.setExecutionREF_(executionCommand);
		po.setExecutionType_(ExecutionType.SCRIPT);
		po.setName_(name);
		System.out.println(po.toString());
		return po;
	}
	
	public static PrimitiveOperation newScriptFromURLType(String name, String scriptRelativePath){
		PrimitiveOperation po = new PrimitiveOperation();	
		po.setExecutionREF_(scriptRelativePath);
		po.setExecutionType_(ExecutionType.SCRIPT);
		po.setName_(name);
		System.out.println(po.toString());
		return po;
	}
	
	public static PrimitiveOperation newSalsaConnector(String name, String connectorName){
		PrimitiveOperation po = new PrimitiveOperation();	
		po.setExecutionREF_(connectorName);
		po.setExecutionType_(ExecutionType.SALSA_CONNECTOR);
		po.setName_(name);
		System.out.println(po.toString());
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

	public void setName_(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "PrimitiveOperation [name=" + name + ", executionType=" + executionType + ", executionREF=" + executionREF + "]";
	}
	
}
