/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.neo4jAccess;

import at.ac.tuwien.dsg.comot.elise.service.settings.EliseConfiguration;
import javax.ws.rs.core.Context;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hungld
 */
@Configuration
@EnableNeo4jRepositories
@ComponentScan
@Transactional
public class AppContext  extends Neo4jConfiguration {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    public AppContext() {
	setBasePackage("at.ac.tuwien.dsg.comot");
    }
//    @Autowired
//    protected GraphDatabaseService db;

    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
        //return new SpringRestGraphDatabase(EliseConfiguration.DATA_BASE_SEPARATE_ENDPOINT);
    }
}
