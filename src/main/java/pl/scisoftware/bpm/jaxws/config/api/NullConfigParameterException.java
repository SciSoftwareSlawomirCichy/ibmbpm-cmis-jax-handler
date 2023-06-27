package pl.scisoftware.bpm.jaxws.config.api;

public class NullConfigParameterException extends ConfigException {

	private static final long serialVersionUID = -8624846137640299910L;

	public NullConfigParameterException(String parameterName) {
		super(String.format("Configuration's parameter '%s' can't be null", parameterName));
	}

}
