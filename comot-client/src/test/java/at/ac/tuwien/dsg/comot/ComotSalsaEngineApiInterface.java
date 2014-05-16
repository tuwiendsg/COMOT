package at.ac.tuwien.dsg.comot;

import at.ac.tuwien.dsg.cloud.salsa.common.interfaces.SalsaEngineApiInterface;

import javax.ws.rs.core.Response;

/**
 * @author omoser
 */
public interface ComotSalsaEngineApiInterface extends SalsaEngineApiInterface {

    Response fetchStatus(String serviceId);
}
