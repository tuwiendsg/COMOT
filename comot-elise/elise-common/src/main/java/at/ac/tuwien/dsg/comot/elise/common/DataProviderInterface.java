/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common;

import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Entity;
import java.util.Properties;

/**
 *
 * @author hungld
 */
public interface DataProviderInterface {
    Entity readData();
}
