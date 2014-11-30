package at.ac.tuwien.dsg.comot.common.model.monitoring;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ElementMonitoring {

	protected String id;
	protected Type type;
	protected Date timestamp;
	protected int hashCode;
	protected List<Metric> metrics = new ArrayList<>();
	protected List<ElementMonitoring> children = new ArrayList<>();

	public enum Type {
		SERVICE,
		TOPOLOGY,
		UNIT, VM
	}

	public ElementMonitoring() {
	}

	public void addChild(ElementMonitoring element) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(element);
	}

	public void addMetric(Metric metric) {
		if (metrics == null) {
			metrics = new ArrayList<>();
		}
		metrics.add(metric);
	}

	// GENERATED METHODS

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public List<ElementMonitoring> getChildren() {
		return children;
	}

	public void setChildren(List<ElementMonitoring> children) {
		this.children = children;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public int getHashCode() {
		return hashCode;
	}

	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

}
