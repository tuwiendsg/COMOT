package at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories;

import java.util.Set;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;

@RepositoryRestResource(collectionResourceRel = "cloudofferedserviceunit", path = "cloudofferedserviceunit")
public interface CloudOfferredServiceRepository extends GraphRepository<CloudOfferedServiceUnit>, RelationshipOperationsRepository<CloudOfferedServiceUnit> {
	
	@Query("match (n:CloudOfferedServiceUnit) where n.name={name} return n")
	CloudOfferedServiceUnit findByName(@Param(value = "name") String name);
	
	@Query("match (n:CloudOfferedServiceUnit)-[:"+LinkType.CLOUD_OFFER_SERVICE_BELONGS_TO_PROVIDER+"]->(p:CloudProvider) where p.name={name} return n")
    Set<CloudOfferedServiceUnit> findByProvider(@Param(value = "name") String name);
	
	
}
	