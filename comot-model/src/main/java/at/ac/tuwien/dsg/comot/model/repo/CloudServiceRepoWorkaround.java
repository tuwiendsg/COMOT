package at.ac.tuwien.dsg.comot.model.repo;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;

@Component
public class CloudServiceRepoWorkaround {

	protected static final Logger log = LoggerFactory.getLogger(CloudServiceRepoWorkaround.class);

	@Autowired
	protected CloudServiceRepo csRepo;

	@Autowired
	protected ServiceUnitRepo stackNodeRepo;

	/**
	 * Saves all stack nodes separately before saving the entire service
	 * 
	 * @param service
	 */
	@Transactional
	public CloudService save(CloudService service) {

		doTopologies(service.getServiceTopologies());

		return csRepo.save(service);
	}

	protected void doTopologies(Set<ServiceTopology> topos) {

		Set<ServiceUnit> savedNodes;

		for (ServiceTopology topo : topos) {
			savedNodes = new HashSet<>();

			for (ServiceUnit node : topo.getServiceUnits()) {
				savedNodes.add(stackNodeRepo.save(node));
			}

			topo.setServiceUnits(savedNodes);

			doTopologies(topo.getServiceTopologies());
		}

	}

}
