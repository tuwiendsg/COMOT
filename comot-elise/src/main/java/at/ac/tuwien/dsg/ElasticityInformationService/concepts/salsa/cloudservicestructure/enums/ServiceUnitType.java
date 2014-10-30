package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ServiceUnitType")
@XmlEnum
public enum ServiceUnitType {
	VIRTUAL_MACHINE("vm"),
	APPLICATION_CONTAINER("app_container"),
	WEB_CONTAINER("web_container"),
	EXECUTABLE("executable"),
	WEB_APPLICATION("web_app");
	
	private String entityType;
	
	private ServiceUnitType(String entityType){
		this.entityType = entityType;
	}

	public String getEntityTypeString() {
		return entityType;
	}
	
	public static ServiceUnitType fromString(String text) {
	    if (text != null) {
	      for (ServiceUnitType b : ServiceUnitType.values()) {
	        if (text.equalsIgnoreCase(b.getEntityTypeString())) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }

}
