package at.ac.tuwien.dsg.elise.extension.collectors.flexiant;



public enum FlexiantParameterStrings {
	EMAIL("email"),
	CUSTOMER_UUID("customerUUID"),	
	PASSWORD("password"),
	ENDPOINT("endpoint"),
	VDC_UUID("vdcUUID"),
	DEFAULT_PRODUCT_OFFER_UUID("defaultProductOfferUUID"),
	CLUSTER_UUID("clusterUUID"),
	NETWORK_UUID("networkUUID"),
	SSH_KEY("sshkey");
	
	private String value;
	
	private FlexiantParameterStrings(String value){
		this.value = value;
	}
	
	public String getString() {
		return value;
	}

	@Deprecated
	public static FlexiantParameterStrings fromString(String text) {
	    if (text != null) {
	      for (FlexiantParameterStrings b : FlexiantParameterStrings.values()) {
	        if (text.equalsIgnoreCase(b.getString())) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }

}
