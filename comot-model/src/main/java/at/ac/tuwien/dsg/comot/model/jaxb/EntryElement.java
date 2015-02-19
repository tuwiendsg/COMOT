package at.ac.tuwien.dsg.comot.model.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class EntryElement {

	@XmlAttribute
	public String key;
	@XmlAttribute
	public String value;

	public EntryElement() {
	}

	public EntryElement(String key, String value) {
		this.key = key;
		this.value = value;
	}
}