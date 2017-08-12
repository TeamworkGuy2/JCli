package twg2.cli;

/**
 * @author TeamworkGuy2
 * @since 2015-4-30
 */
public class ParameterParserResult {
	/** null if the parameter was parsed successfully, non-null if there was a parsing or validation error */
	private ParameterParserException parseError;
	private ParameterType resultType;
	private boolean isParseResultAnArray;
	private Object parseResult;


	public ParameterParserResult(ParameterParserException parseError) {
		this.parseError = parseError;
	}


	public ParameterParserResult(ParameterType type, Object parseResult, boolean isParseResultAnArray) {
		this.resultType = type;
		this.parseResult = parseResult;
		this.isParseResultAnArray = isParseResultAnArray;
	}


	public boolean isError() {
		return parseError != null;
	}


	public ParameterParserException getParseError() {
		return parseError;
	}


	public ParameterType getParseResultType() {
		return resultType;
	}


	public boolean isParseResultAnArray() {
		return isParseResultAnArray;
	}


	public Object getParseResult() {
		return parseResult;
	}

}
