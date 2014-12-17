/** 
   Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group E184

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package at.ac.tuwien.dsg.elise.concepts.sybl.elasticityInformation;

import java.io.Serializable;

public class SYBLAnnotation implements Serializable{
	private static final long serialVersionUID = 1L;
private String monitoring;
private String constraints;
private String strategies;
private String priorities;
private AnnotationType annotationType;
private String entityID;
public static enum AnnotationType{
	   CLOUD_SERVICE, SERVICE_UNIT, SERVICE_TOPOLOGY, CODE_REGION,RELATIONSHIP;
	 }
public String getStrategies() {
	return strategies;
}
public void setStrategies(String strategies) {
	this.strategies = strategies;
}
public String getPriorities() {
	return priorities;
}
public void setPriorities(String priorities) {
	this.priorities = priorities;
}
public String getConstraints() {
	return constraints;
}
public void setConstraints(String constraints) {
	this.constraints = constraints;
}
public String getMonitoring() {
	return monitoring;
}
public void setMonitoring(String monitoring) {
	this.monitoring = monitoring;
}
public AnnotationType getAnnotationType() {
	return annotationType;
}
public void setAnnotationType(AnnotationType annotationType) {
	this.annotationType = annotationType;
}
public String getEntityID() {
	return entityID;
}
public void setEntityID(String entityID) {
	this.entityID = entityID;
}
}
