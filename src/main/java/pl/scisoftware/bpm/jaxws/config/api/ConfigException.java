package pl.scisoftware.bpm.jaxws.config.api;

public class ConfigException extends Exception {

	private static final long serialVersionUID = -2070995982120807062L;

	public ConfigException() {
		super();
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

}
