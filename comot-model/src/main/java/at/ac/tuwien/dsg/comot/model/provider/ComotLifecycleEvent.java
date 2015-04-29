package at.ac.tuwien.dsg.comot.model.provider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ComotLifecycleEvent extends PrimitiveOperation {

	private static final long serialVersionUID = -9094347746546330735L;


	public ComotLifecycleEvent() {

	}

	public ComotLifecycleEvent(String name, String executeMethod) {
		super(name, executeMethod);
	}
}
