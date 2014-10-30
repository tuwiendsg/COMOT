package at.ac.tuwien.dsg.ElasticityInformationService.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.EntityRepository;

@Configuration
@EnableNeo4jRepositories(basePackages = "at.ac.tuwien.dsg.ElasticityInformationService")
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
public class client{
	@Autowired
	static EntityRepository repo;
	
	public static void main(String[] args) {
		
//		System.out.println("Client ---> \n");
//		System.out.println(repo.findByName("SUSU2").getName() + " --- \n");
//		System.out.println(repo.findByName("SUSU1").getType());
	}

}
