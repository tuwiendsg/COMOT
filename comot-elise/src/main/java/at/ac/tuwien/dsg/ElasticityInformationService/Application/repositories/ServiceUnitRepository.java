package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnit;

@RepositoryRestResource(collectionResourceRel = "serviceunit", path = "serviceunit")
public interface ServiceUnitRepository extends GraphRepository<ServiceUnit>, RelationshipOperationsRepository<ServiceUnit> { 
    ServiceUnit findByName(String name);
    Iterable<ServiceUnit> findByRelatedServiceunitsName(String name);	
}
// guide: http://spring.io/guides/gs/accessing-neo4j-data-rest/