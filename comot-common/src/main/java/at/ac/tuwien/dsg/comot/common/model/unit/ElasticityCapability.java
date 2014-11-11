package at.ac.tuwien.dsg.comot.common.model.unit;


public class ElasticityCapability {

	protected String name;
	protected String script;

	public ElasticityCapability(String name, String script) {
		super();
		this.name = name;
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
