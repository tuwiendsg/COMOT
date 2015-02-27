package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "List")
public class JaxbList<T> {

	@XmlElement(name = "list")
	protected List<T> list;

	public JaxbList() {
	}

	public JaxbList(List<T> list) {
		this.list = list;
	}

	public JaxbList(Set<T> list) {
		this.list = new ArrayList<T>(list);
	}

	public List<T> getList() {
		return list;
	}
}
