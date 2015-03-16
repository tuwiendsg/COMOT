/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.neo4jAccess;

import at.ac.tuwien.dsg.comot.model.provider.Entity;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 *
 * @author hungld
 */
public interface EntityRepository extends GraphRepository<Entity> {


}
