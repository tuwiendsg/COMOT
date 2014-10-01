package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;

@RepositoryRestResource(collectionResourceRel = "cloudOfferedServiceunit", path = "cloudOfferedServiceunit")
public interface CloudOfferredServiceRepository extends GraphRepository<CloudOfferedServiceUnit>, RelationshipOperationsRepository<CloudOfferedServiceUnit> { 
	CloudOfferedServiceUnit findByName(String name);
    Iterable<CloudOfferedServiceUnit> findByProvider(String name);
}
