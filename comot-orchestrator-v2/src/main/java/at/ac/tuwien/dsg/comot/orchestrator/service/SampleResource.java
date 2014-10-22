package at.ac.tuwien.dsg.comot.orchestrator.service;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// http://localhost:8380/rest/sample/one/aaaaaaaaaaaaaaaaaaaaaa

@Service
@Path("/sample")
public class SampleResource {
	
	private static final Logger log = LoggerFactory.getLogger(SampleResource.class);

	
	@PostConstruct
	public void aaa(){
		log.info("REST resource created");
	}
	
	@GET
	@Path("/one/{one}")
	@Consumes(MediaType.WILDCARD)
	public String doSomething(@PathParam("one") String one){
		log.info("param: {}", one);
		return one;
	}

}
