/**
 * Copyright 2013 Technische Universitaet Wien (TUW), Distributed Systems Group
 * E184
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.ac.tuwien.dsg.ElasticityInformationService.concepts;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class Entity implements Serializable {

    @GraphId
    public Long id;
   
    @Indexed(unique=true)
    public String name;
    
    public String type;
    
    @RelatedTo(type="RelatedEntity", direction=Direction.BOTH)
    public Set<Entity> relatedNodes;

   

    public Entity() {
    }
   
    public Entity(String name) {
        this.name = name;
    }

    public Entity(long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
    	if (type==null){
    		type = "Entity";
    	}
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Entity clone() {
        return new Entity(id, name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entity other = (Entity) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
    	  StringBuilder result = new StringBuilder();
    	  String newLine = System.getProperty("line.separator");

    	  result.append( this.getClass().getName() );
    	  result.append( " Object {" );
    	  result.append(newLine);

    	  //determine fields declared in this class only (no fields of superclass)
    	  Field[] fields = this.getClass().getDeclaredFields();

    	  //print field names paired with their values
    	  for ( Field field : fields  ) {
    		field.setAccessible(true);
    	    result.append("  ");
    	    try {
    	      result.append( field.getName() +"(" + field.getType().getSimpleName() +")" );
    	      result.append(": ");
    	      //requires access to private field:
    	      result.append( field.get(this) );
    	    } catch ( IllegalAccessException ex ) {
    	      System.out.println(ex);
    	    }
    	    result.append(newLine);
    	  }
    	  result.append("}");

    	  return result.toString();
    	}
    
    
    
}
