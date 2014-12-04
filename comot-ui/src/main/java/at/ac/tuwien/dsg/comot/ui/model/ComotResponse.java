package at.ac.tuwien.dsg.comot.ui.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class ComotResponse implements Serializable {

	private static final long serialVersionUID = 6262604538342475947L;

	protected String id;

	public ComotResponse() {

	}

	public ComotResponse(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
