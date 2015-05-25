/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.UnitInstanceDAO;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;
import java.util.Collections;
import java.util.Set;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author hungld
 */
public abstract class UnitInstanceCollector extends GenericCollector {

    UnitInstanceDAO unitInstanceDAO = (UnitInstanceDAO) JAXRSClientFactory.create(this.endpoint, UnitInstanceDAO.class, Collections.singletonList(new JacksonJsonProvider()));

    public UnitInstanceCollector(String collectorName) {
        super(collectorName);
    }

    @Override
    public void sendData() {
        logger.debug("Inside the sending data method ...");
        Set<UnitInstance> instances = collect();
        logger.debug("Prepare to send items, Number: " + instances.size());
        for (UnitInstance i : instances) {
            System.out.println("Adding unit instance: " + i.getName() +"/" + i.getId());
            ServiceIdentification si = identify(i);
            i.setIdentification(si.toJson());
            this.unitInstanceDAO.addUnitInstance(i);
        }
    }

    @Override
    public abstract Set<UnitInstance> collect();

    public abstract ServiceIdentification identify(UnitInstance paramUnitInstance);
}
