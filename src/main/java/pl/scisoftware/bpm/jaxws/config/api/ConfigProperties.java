package pl.scisoftware.bpm.jaxws.config.api;

public class ConfigProperties {

	private ConfigProperties() {
	}

	public static final String DEFAULT_SSL_PROTOCOL = "TLSv1.2";
	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String PROPERTY_USERNAME = ".username";
	@SuppressWarnings("squid:S2068")
	public static final String PROPERTY_PASSWORD = ".password";

}
