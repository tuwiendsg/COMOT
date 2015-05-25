/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.ProviderDAO;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Provider;
import java.util.Collections;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author hungld
 */
public abstract class ProviderCollector extends GenericCollector {

    public ProviderCollector(String collectorName) {
        super(collectorName);
    }

    ProviderDAO providerDAO = (ProviderDAO) JAXRSClientFactory.create(this.endpoint, ProviderDAO.class, Collections.singletonList(new JacksonJsonProvider()));

    public void sendData() {
        Provider p = collect();

        this.providerDAO.addProvider(p);
    }

    public abstract Provider collect();
}
