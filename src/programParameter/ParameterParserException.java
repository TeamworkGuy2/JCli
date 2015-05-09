package programParameter;

/**
 * @author TeamworkGuy2
 * @since 2015-4-30
 */
public class ParameterParserException {
	private ParameterParserExceptionType errorType;
	private String message;
	private Throwable cause;


	/**
	 * @param message
	 * @param cause
	 * @param errorType
	 */
	public ParameterParserException(ParameterParserExceptionType errorType, String message, Throwable cause) {
		this.errorType = errorType;
		this.message = message;
		this.cause = cause;
	}


	public ParameterParserExceptionType getParseErrorType() {
		return errorType;
	}


	public String getMessage() {
		return message;
	}


	public Throwable getCause() {
		return cause;
	}

}
