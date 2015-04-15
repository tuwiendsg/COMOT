package at.ac.tuwien.dsg.comot.model.provider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ComotCustomEvent extends PrimitiveOperation {

	private static final long serialVersionUID = -9094347746546330735L;
	@XmlAttribute
	protected Boolean additionalInput;

	public ComotCustomEvent() {

	}

	public ComotCustomEvent(String name, String executeMethod, Boolean additionalInput) {
		super(name, executeMethod);
		this.additionalInput = additionalInput;
	}

	public Boolean getAdditionalInput() {
		return additionalInput;
	}

	public void setAdditionalInput(Boolean additionalInput) {
		this.additionalInput = additionalInput;
	}

}
