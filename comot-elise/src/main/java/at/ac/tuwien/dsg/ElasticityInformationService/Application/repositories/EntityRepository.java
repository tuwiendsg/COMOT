package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;

@RepositoryRestResource(collectionResourceRel = "entity", path = "entity")
public interface EntityRepository extends GraphRepository<Entity>, RelationshipOperationsRepository<Entity> {
	Entity findByName(String name);
    Iterable<Entity> findByRelatedNodes(String name);	    
}
