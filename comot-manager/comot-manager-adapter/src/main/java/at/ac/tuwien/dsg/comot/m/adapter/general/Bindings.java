package at.ac.tuwien.dsg.comot.m.adapter.general;

import java.util.HashSet;
import java.util.Set;

public class Bindings {

	protected Set<String> lifecycle = new HashSet<>();
	protected Set<String> custom = new HashSet<>();
	protected Set<String> exception = new HashSet<>();

	public Bindings addLifecycle(String routingKey) {
		lifecycle.add(routingKey);
		return this;
	}

	public Bindings addCustom(String routingKey) {
		custom.add(routingKey);
		return this;
	}

	public Bindings addException(String routingKey) {
		exception.add(routingKey);
		return this;
	}

	public Set<String> getLifecycle() {
		return lifecycle;
	}

	public Set<String> getCustom() {
		return custom;
	}

	public Set<String> getException() {
		return exception;
	}

}
