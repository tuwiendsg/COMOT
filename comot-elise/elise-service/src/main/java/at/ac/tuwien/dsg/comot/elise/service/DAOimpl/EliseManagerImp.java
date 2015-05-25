/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.DAOimpl;

import at.ac.tuwien.dsg.comot.elise.common.EliseManager.CollectorDescription;
import at.ac.tuwien.dsg.comot.elise.common.EliseManager.EliseManager;
import at.ac.tuwien.dsg.comot.elise.service.Communication.EliseCommunicationService;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.OfferedServiceRepository;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import at.ac.tuwien.dsg.comot.elise.service.utils.IdentificationManager;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hungld
 */
public class EliseManagerImp implements EliseManager {
    Logger logger = EliseConfiguration.logger;

   // FOR MANAGING COLLECTORS
    List<CollectorDescription> collectors = new ArrayList<>();

    @Override
    public String registerCollector(CollectorDescription collector) {
        logger.debug("Registering a collector");
        if (this.collectors==null){
            this.collectors = new ArrayList<>();
        }
        this.collectors.add(collector);
        logger.debug("Registered a collector: " + collector.getCollectorID() );
        return "ELISE registered a new collector: " + collector.getCollectorID();
    }

    @Override
    public String updateColector(CollectorDescription collector) {
        if (removeCollector(collector.getCollectorID()) != null) {
            return "ELISE updated an existed collector" + registerCollector(collector);
        }
        return null;
    }

    @Override
    public CollectorDescription getColector(String collectorID) {
        for (CollectorDescription desp : this.collectors) {
            if (desp.getCollectorID().equals(collectorID)) {
                return desp;
            }
        }
        return null;
    }

    @Override
    public String removeCollector(String collectorID) {
        for (CollectorDescription desp : this.collectors) {
            if (desp.getCollectorID().equals(collectorID)) {
                this.collectors.remove(desp);
                return "ELISE removed a collector" + collectorID;
            }
        }
        return null;
    }

    @Override
    public List<CollectorDescription> getCollectorList() {
        return this.collectors;
    }

    // FOR GENERAL TASKS
    @Autowired
    OfferedServiceRepository surepo;

    @Override
    public String health() {
        System.out.println("Health checked");
        return "healthy";
    }

    @Override
    public String cleanDB() {
        surepo.cleanDataBase();
        return "DB Cleaned";
    }

    // FOR IDENTIFICATION
    @Override
    public String updateComposedIdentification(ServiceIdentification si) {
        IdentificationManager im = new IdentificationManager();
        return im.searchAndUpdate(si);
    }

}
