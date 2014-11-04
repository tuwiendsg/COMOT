package at.ac.tuwien.dsg.elise.extension.collectors;

public enum OpenStackParameterStrings {
	PORT("port"),
	SSH_KEY_NAME("sshKeyName"),
	END_POINT("end_point"),
	USERNAME("username"),
	PASSWORD("password"),
	TENANT("tenant"),
	KEYSTONE_ENDPOINT("keystone_endpoint");
	
	private String value;
	
	private OpenStackParameterStrings(String value){
		this.value = value;
	}
	
	public String getString() {
		return value;
	}

	@Deprecated
	public static OpenStackParameterStrings fromString(String text) {
	    if (text != null) {
	      for (OpenStackParameterStrings b : OpenStackParameterStrings.values()) {
	        if (text.equalsIgnoreCase(b.getString())) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }

}
