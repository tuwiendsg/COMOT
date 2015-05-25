package at.ac.tuwien.dsg.comot.elise.service.neo4jAccess;

import at.ac.tuwien.dsg.comot.model.elasticunit.generic.FeatureType;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.GenericServiceUnit;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Resource;
import java.util.Set;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface OfferedServiceRepository extends GraphRepository<GenericServiceUnit> {
//, RelationshipOperationsRepository<OfferedServiceUnit> {
        
        @Query("match (n:OfferedServiceUnit) return n")
	Set<GenericServiceUnit> listServiceUnit();
	
	@Query("match (n:OfferedServiceUnit) where n.name={name} return n")
	Set<GenericServiceUnit> findByName(@Param(value = "name") String name);
        
        @Query("match (n:OfferedServiceUnit) where n.id={id} return n")
	GenericServiceUnit findByUniqueID(@Param(value = "id") String id);
	
        @Query("match (p:Provider)-->(n:OfferedServiceUnit) where p.id={providerID} return n")
        Set<GenericServiceUnit> findByProviderID(@Param(value = "providerID") String providerID);
        
        @Query("match (s:OfferedServiceUnit)-->(r:Resource)-->(t:ResourceOrQualityType) where s.id={serviceID} and t.id={resourceTypeID} return r")
        Set<Resource> findResourceOfOfferedServiceUnitByType(@Param(value = "serviceID") String serviceID, @Param(value = "resourceTypeID") String resourceTypeID);
        
        @Query("match (s:OfferedServiceUnit)-->(r:Resource) where s.id={serviceID} and r.id={resourceID} return r")
        Resource findResourceOfOfferedServiceUnitByID(@Param(value = "serviceID") String serviceID, @Param(value = "resourceID") String resourceID);
        
        @Query("match (r:Resource)-[hasMetric]->(mv:MetricValue) where r.id={resourceID} return mv")
        Set<MetricValue> getResourceMetricDetails(String resourceID);
        
        @Query("match (n:OfferedServiceUnit)-->(m)-->(t:FeatureType) where n.id={serviceUniqueID} return distinct t")
        Set<FeatureType> findResourceOrQualityTypes(@Param(value = "serviceUniqueID") String serviceUniqueID);
        
	@Query("match (n:OfferedServiceUnit) where n.subcategory={subcategory} return n")
	Set<GenericServiceUnit> findBySubcategory(@Param(value = "subcategory") String subcategory);   
        	
	@Query("match (n) optional match (n)-[r]-() delete n,r")
	void cleanDataBase();
}
