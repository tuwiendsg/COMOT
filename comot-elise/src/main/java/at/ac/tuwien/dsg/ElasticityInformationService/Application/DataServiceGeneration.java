package at.ac.tuwien.dsg.ElasticityInformationService.Application;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.GraphDatabase;

import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.CloudOfferredServiceRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.EntityRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.CloudProvider;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.ResourceType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.ResourceValue;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.monitoringConcepts.MetricValue;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.PrimitiveOperation;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnit;
import at.ac.tuwien.dsg.ElasticityInformationService.settings.EliseConfiguration;

@Configuration
@EnableNeo4jRepositories(basePackages = "at.ac.tuwien.dsg.ElasticityInformationService")
//@Import(RepositoryRestMvcConfiguration.class)
//@EnableAutoConfiguration
public class DataServiceGeneration extends Neo4jConfiguration implements CommandLineRunner {

    public DataServiceGeneration() {
        setBasePackage("at.ac.tuwien.dsg.ElasticityInformationService");
    }

    @Bean
    GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
    }

    @Autowired
    ServiceUnitRepository suRepository;
    
    @Autowired
    CloudOfferredServiceRepository vmRepo;
    
    @Autowired
    EntityRepository enRepo;

    @Autowired
    GraphDatabase graphDatabase;

    public void run(String... args) throws Exception {
    	generateOpenStack();    
    }
    public static void main(String[] args) throws Exception {
        FileUtils.deleteRecursively(new File("accessingdataneo4j.db"));
        SpringApplication.run(DataServiceGeneration.class, args);
    }
    
    private void generateOpenStack(){
    	System.out.println("Create database for OpenStack DSG");
    	Transaction tx = graphDatabase.beginTx();
    	CloudProvider os = new CloudProvider("dsg@openstack", "IAAS");    	
    	enRepo.save(os);
    	tx.success();
    	
    	ServiceUnit osVm = new ServiceUnit();
    	osVm.setName("OpenstackVM");
    	osVm.addCapability("vm");
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("start", "openstack"));
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("stop", "openstack"));
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("addFloatIP", "openstack"));
    	
    	suRepository.save(osVm);
    	tx.success();
    	System.out.println("Do the generation");
    	
    	// resource of the cloud provider
    	ResourceType rComputing = new ResourceType("Computing");
    	ResourceType rMemory = new ResourceType("Memory");
    	ResourceType rRootDisk = new ResourceType("RootDisk");
    	ResourceType rEphemeralDisk = new ResourceType("EphemeralDisk");
        enRepo.save(rComputing);        
        enRepo.save(rMemory);        
        enRepo.save(rRootDisk);        
        enRepo.save(rEphemeralDisk);
    	
    	//m1.tiny
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.tiny");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

            //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(1));    
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(512));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(0));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(0));
                utility.addResourceProperty(reRela);
            }            
            vmRepo.save(utility);      
        }

    	//m1.micro
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.micro");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(1));    
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(960));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(20));
                utility.addResourceProperty(reRela);
            }       
            vmRepo.save(utility);      
        }
        
      //m1.small
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.small");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(1));     
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(1920));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(20));
                utility.addResourceProperty(reRela);
            }           
            vmRepo.save(utility);      
        }
        
      //m1.medium
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.medium");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(2));    
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(3750));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }           
            vmRepo.save(utility);      
        }
        
      //m2.medium
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m2.medium");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(3));    
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(5760));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }         
            vmRepo.save(utility);      
        }
        
      //m1.small
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.small");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(1));    
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(960));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(20));
                utility.addResourceProperty(reRela);
            }       
            vmRepo.save(utility);      
        }
        
      //m1.large
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.large");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(4));   
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(7680));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(80));
                utility.addResourceProperty(reRela);
            }           
            vmRepo.save(utility);      
        }
        
      //m1.xlarge
        {
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", "m1.xlarge");
            utility.setDerivedServiceUnit(osVm);
            utility.setProvider(os);

          //vm resource: computing
            {               
                ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rComputing);
                reRela.addProperty(new Metric("VCPU", "number"), new MetricValue(8));      
                reRela.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(reRela);
            }

            //vm resource: memory
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rMemory);
                reRela.addProperty(new Metric("size", "MB"), new MetricValue(15360));
                utility.addResourceProperty(reRela);
            }
            //vm resource: RootDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rRootDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(40));
                utility.addResourceProperty(reRela);
            }
            //vm resource: EphemeralDisk
            {
            	ResourceValue reRela = new ResourceValue();            
                reRela.setSource(utility);
                reRela.setTarget(rEphemeralDisk);
                reRela.addProperty(new Metric("size", "GB"), new MetricValue(160));
                utility.addResourceProperty(reRela);
            }       
            vmRepo.save(utility);      
        }
        

    	
    	tx.success();
    	tx.close();
    }
    
}













