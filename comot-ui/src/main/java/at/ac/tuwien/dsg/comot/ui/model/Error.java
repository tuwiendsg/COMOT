package at.ac.tuwien.dsg.comot.ui.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Error implements Serializable {

	private static final long serialVersionUID = 6692918664587778374L;

	protected String message;
	protected String origin;

	public Error() {

	}

	public Error(String message, String origin) {
		super();
		this.message = message;
		this.origin = origin;
	}

	public Error(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@Override
	public String toString() {

		String msg = "";

		if (origin != null) {
			msg = "component: " + origin;
		}

		if (message != null) {
			if (!msg.equals("")) {
				msg += "\n";
			}
			msg += "message: " + message;
		}

		return msg;
	}

}