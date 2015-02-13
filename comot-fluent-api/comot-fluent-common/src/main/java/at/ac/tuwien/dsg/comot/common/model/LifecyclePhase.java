/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.common.model;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public enum LifecyclePhase {

    DEPLOY("deploy"),
    UNDEPLOY("undeploy"),
    START("start"),
    STOP("stop");

    final String action;

    LifecyclePhase(String type) {
        this.action = type;
    }

    @Override
    public String toString() {
        return action;
    }
}
