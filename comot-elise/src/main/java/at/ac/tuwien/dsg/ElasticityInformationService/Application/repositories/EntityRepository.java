package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;

@Component
@RepositoryRestResource(collectionResourceRel = "entity", path = "entity")
public interface EntityRepository extends GraphRepository<ServiceEntity> {
	
	@Query("match (n:Entity) where n.name={name} return n")
	ServiceEntity findByName(@Param(value = "name") String name);
	
	@Query("match (n:Entity) where n.type={type} return n")
	Iterable<ServiceEntity> findByType(@Param(value = "type") String type);
	    
}

// http://docs.spring.io/spring-data/neo4j/docs/current/reference/html/
// http://spring.io/guides/gs/accessing-neo4j-data-rest/
// http://java.dzone.com/articles/domain-modeling-spring-data
