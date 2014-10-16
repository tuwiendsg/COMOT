/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentService;

public class SalsaInterraction {

	private static final Logger log = LoggerFactory.getLogger(SalsaInterraction.class);
	
	private static final long CHECK_STATE_TIMEOUT = 10000;

	private DeploymentService depServ;

	public SalsaInterraction(DeploymentService depServ) {
		this.depServ = depServ;
	}

	public void waitUntilRunning(String serviceID) throws CoreServiceException {

		boolean inProgress = true;
		CloudService service;

		do {

			try {
				Thread.sleep(CHECK_STATE_TIMEOUT);
			} catch (InterruptedException ex) {
				log.error(ex.getMessage(), ex);
			}

			inProgress = true;
			service = depServ.getStatus(serviceID);

			log.info("Waiting until service " + serviceID + " is running. Now in state={}", service.getState());

			if (service.getState().equals(SalsaEntityState.DEPLOYED)
					|| service.getState().equals(SalsaEntityState.RUNNING)) {
				inProgress = false;

				for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service
						.getAllComponentByType(SalsaEntityType.SOFTWARE)) {

					if (serviceUnit != null
							&& !(serviceUnit.getState().equals(SalsaEntityState.DEPLOYED) || serviceUnit.getState()
									.equals(SalsaEntityState.RUNNING))) {
						inProgress = true;
						break;
					}
				}
			}

		} while (inProgress);

	}

	public void setDeploymentService(DeploymentService depServ) {
		this.depServ = depServ;
	}
	
	

}
