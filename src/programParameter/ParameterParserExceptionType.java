package programParameter;

enum ParameterParserExceptionType {
	INPUT_PARSING_EXCEPTION, // if the input could not be parsed
	SETTER_EXCEPTION, // the setter function call threw an exception (probably code related issue)
	VALIDATOR_EXCEPTION, // the validator function call threw an exception (probably code related issue)
	INVALID_PARSED_INPUT, // the validator returned false
	VALIDATOR_MESSAGE_GENERATOR_EXCEPTION // the validator message generator threw an exception (probably code related issue)
}