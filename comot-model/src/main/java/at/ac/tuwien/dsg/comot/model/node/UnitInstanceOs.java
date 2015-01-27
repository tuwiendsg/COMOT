package at.ac.tuwien.dsg.comot.model.node;

import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.type.State;

@XmlRootElement
public class UnitInstanceOs extends UnitInstance {

	private static final long serialVersionUID = 7489425455903961743L;

	protected String provider;
	protected String baseImage;
	protected String instanceType;
	protected String uuid;
	protected String ip;

	public UnitInstanceOs() {

	}

	public UnitInstanceOs(String id, int instanceId, UnitInstance hostInstance, State state,
			String provider,
			String baseImage,
			String instanceType, String uuid, String ip) {
		super(id, instanceId, state, hostInstance);
		this.provider = provider;
		this.baseImage = baseImage;
		this.instanceType = instanceType;
		this.uuid = uuid;
		this.ip = ip;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getBaseImage() {
		return baseImage;
	}

	public void setBaseImage(String baseImage) {
		this.baseImage = baseImage;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
