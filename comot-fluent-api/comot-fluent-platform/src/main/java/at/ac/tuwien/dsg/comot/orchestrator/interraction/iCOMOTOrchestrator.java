/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator.interraction;

import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.govops.GovOpsInterraction;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class iCOMOTOrchestrator extends COMOTOrchestrator {

    private GovOpsInterraction govOpsInterraction;

    {
        govOpsInterraction = new GovOpsInterraction();
    }

    public iCOMOTOrchestrator() {
    }

    public iCOMOTOrchestrator(String ip) {
        super(ip);
    }

    public iCOMOTOrchestrator withGovOpsIP(String ip) {
        govOpsInterraction.setIp(ip);
        return this;
    }

    public iCOMOTOrchestrator withGovOpsPort(Integer port) {
        govOpsInterraction.setPort(port);
        return this;
    }

    public void enforceCapabilityOnSingleUnit(ServiceUnit unit, SensorCapability capability) {
        govOpsInterraction.enforceCapabilityOnSingleUnit(unit, capability.toString());
    }

    public void enforceCapabilityOnClassOfUnits(ServiceUnit unit, SensorCapability capability) {

        govOpsInterraction.enforceCapabilityOnClassOfUnits(unit, capability.toString());
    }

    public void enforceCapabilityOnSingleUnit(ServiceUnit unit, SensorCapability capability, String args) {
        govOpsInterraction.enforceCapabilityOnSingleUnit(unit, capability.toString(), args);
    }

    public void enforceCapabilityOnClassOfUnits(ServiceUnit unit, SensorCapability capability, String args) {
        govOpsInterraction.enforceCapabilityOnClassOfUnits(unit, capability.toString(), args);
    }

    public enum SensorCapability {

        START("cStartStopSensor/start"),
        STOP("cStartStopSensor/stop"),
        UPDATE_MON_FREQ("cChangeSensorRate/");
        private final String text;

        /**
         * @param text
         */
        private SensorCapability(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

}
