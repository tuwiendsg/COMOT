package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnit;

@RepositoryRestResource(collectionResourceRel = "serviceunit", path = "serviceunit")
public interface ServiceUnitRepository extends GraphRepository<ServiceUnit>, RelationshipOperationsRepository<ServiceUnit> {
	
	@Query("match (n:ServiceUnit) where n.name={name} return n")
	ServiceUnit findByName(@Param(value = "name") String name);
	
	Iterable<ServiceUnit> findByRelatedServiceunitsName(@Param("name") String name);	
}
