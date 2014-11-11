package at.ac.tuwien.dsg.comot.common.model.unit;

import javax.xml.bind.annotation.XmlAttribute;

public class AssociatedVM {

	protected String ip;
	protected String uuid;

	public AssociatedVM(String ip, String uuid) {
		super();
		this.ip = ip;
		this.uuid = uuid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
