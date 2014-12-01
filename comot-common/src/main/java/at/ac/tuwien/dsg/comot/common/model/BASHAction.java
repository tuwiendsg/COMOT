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
public class BASHAction extends AbstractLifecycleAction {

    private String command;

    public BASHAction() {
    }

    public BASHAction(String command) {
        this.command = command;
    }

    public static BASHAction BASHAction(String command) {
        BASHAction action = new BASHAction(command);
        return action;
    }

    public BASHAction withCommand(final String command) {
        this.command = command;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
