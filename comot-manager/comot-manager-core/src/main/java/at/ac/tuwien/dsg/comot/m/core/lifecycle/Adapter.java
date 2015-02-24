package at.ac.tuwien.dsg.comot.m.core.lifecycle;

public interface Adapter {

	public void start(String osuInstanceId, String infoIp, String infoPort);

	public void clean();

}
