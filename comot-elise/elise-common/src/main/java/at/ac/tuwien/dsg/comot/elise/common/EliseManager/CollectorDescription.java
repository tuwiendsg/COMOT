/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.EliseManager;

/**
 *
 * @author hungld
 */
public class CollectorDescription {

    String collectorID;
    String serviceUnitID;
    String collectorName;

    public CollectorDescription() {
    }

    public CollectorDescription(String collectorID, String serviceUnitID, String collectorName) {
        this.collectorID = collectorID;
        this.serviceUnitID = serviceUnitID;
        this.collectorName = collectorName;
    }

    public String getCollectorID() {
        return this.collectorID;
    }

    public String getServiceUnitID() {
        return this.serviceUnitID;
    }

    public String getCollectorName() {
        return this.collectorName;
    }

    public void setCollectorID(String collectorID) {
        this.collectorID = collectorID;
    }

    public void setServiceUnitID(String serviceUnitID) {
        this.serviceUnitID = serviceUnitID;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public String toString() {
        return "CollectorDescription{collectorID=" + this.collectorID + ", serviceUnitID=" + this.serviceUnitID + ", collectorName=" + this.collectorName + '}';
    }
}
