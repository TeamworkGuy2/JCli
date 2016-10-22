package programParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A set of {@link ParameterData} instances to parse against a give set of input values
 * 
 * @param <T> the parameter's name type
 * 
 * @author TeamworkGuy2
 * @since 2014-11-22
 */
public final class ParameterSet<T extends CharSequence> {
	private static int MAX_PARSE_ATTEMPTS = 10;
	/** a unique set of all the parameters in this parameter set */
	private List<ParameterData<T, ? extends Object>> parameters;
	/** a map of parameter names and aliases to their parameters, the same parameter may appear multiple times associated with different names/aliases */
	private Map<T, ParameterData<T, ? extends Object>> parameterNameMap;
	/** the output stream to print the help message to when it is parsed by {@link ParameterSet#parse()} */
	private Appendable outStream;


	public ParameterSet(List<? extends ParameterData<T, ? extends Object>> parameters) {
		this(parameters, false, null, null, (String[])null);
	}


	/** Create a parameter set from a list of parameters
	 * @param parameters the list of parameters to store in this parameter set
	 */
	public ParameterSet(List<? extends ParameterData<T, ? extends Object>> parameters,
			boolean buildHelpParam, String helpParamName, String helpMsg, String... helpParamAliases) {
		this.parameters = new ArrayList<>();
		this.parameters.addAll(parameters);

		if(buildHelpParam) {
			this.parameters.add(createHelpParameter(helpParamName, helpMsg, helpParamAliases));
		}

		this.parameterNameMap = new HashMap<>();

		for(ParameterData<T, ?> parameter : this.parameters) {
			this.parameterNameMap.put(parameter.getPrimaryName(), parameter);
			for(T name : parameter.getAliases()) {
				this.parameterNameMap.put(name, parameter);
			}
		}
	}


	/** Parse an array of inputs and invoke {@link ParameterData#parse(Object[], int, int)} on
	 * the parameters in this parameter set that match any of the inputs.
	 * @param inputs the array of inputs to parse
	 * @param off the offset into {@code inputs} at which to start parsing
	 * @param output the output stream to write information/help messages to
	 * @return a map of all of this parameter set's parameters mapped to true if the
	 * parameter was found in the {@code inputs}, false if a parameter was not in the {@code inputs}
	 */
	public Map<ParameterData<T, Object>, Boolean> parse(T[] inputs, int off, Appendable output) {
		outStream = output;
		ParameterData<T, Object> param = null;
		int paramStart = -1;
		for(int i = off, size = inputs.length; i < size; i++) {
			@SuppressWarnings("unchecked")
			ParameterData<T, Object> paramTemp = (ParameterData<T, Object>)parameterNameMap.get(inputs[i]);
			if(paramTemp != null) {
				param = paramTemp;
				paramStart = i;
				if(paramStart+1 >= inputs.length) {
					param.parse(inputs, paramStart, 1);
				}
				break;
			}
		}

		Map<ParameterData<T, Object>, Boolean> parametersCompleted = new LinkedHashMap<>();
		for(ParameterData<T, ? extends Object> parameter : parameters) {
			@SuppressWarnings("unchecked")
			ParameterData<T, Object> prmtr = (ParameterData<T, Object>)parameter;
			parametersCompleted.put(prmtr, false);
		}
		int nextParamStart = -1;
		for(int i = paramStart + 1, size = inputs.length; i < size; i++) {
			@SuppressWarnings("unchecked")
			ParameterData<T, Object> paramTemp = (ParameterData<T, Object>)parameterNameMap.get(inputs[i]);
			if(paramTemp != null || (i == size - 1 && param != null)) {
				nextParamStart = i + (paramTemp == null && i == size - 1 ? 1 : 0);
				param.parse(inputs, paramStart, nextParamStart - paramStart);
				parametersCompleted.put(param, true);
				paramStart = nextParamStart;
				param = paramTemp;
				if(param != null && i == size - 1) {
					param.parse(inputs, paramStart, 1);
					parametersCompleted.put(param, true);
				}
			}
		}

		return parametersCompleted;
	}


	/** Parse an array of inputs and invoke {@link ParameterSet#parseInteractive(CharSequence[], int, BufferedReader, Appendable, String)} on
	 * the parameters in this parameter set that match any of the inputs.
	 * @see #parseInteractive(CharSequence[], int, BufferedReader, Appendable, String)
	 */
	public void parseInteractive(T[] inputs, BufferedReader input, Appendable output,
			String paramHelpIdentifier) {
		parseInteractive(inputs, 0, input, output, paramHelpIdentifier);
	}


	/** Parse an array of inputs and invoke {@link ParameterSet#getParameterInteractive(ParameterData, BufferedReader, Appendable, String)} on
	 * the parameters in this parameter set that match any of the inputs.
	 * This differs from {@link #parse(CharSequence[], int, Appendable)} because missing parameters
	 * are requested using the specified output stream and parsed from the specified input stream
	 * @param inputs the array of inputs to parse
	 * @param off the offset into {@code inputs} at which to start parsing
	 * @param input the input stream to read user input from
	 * @param output the output stream to print user information and prompts to
	 * @param paramHelpIdentifier the name of the command that causes help information
	 * to be printed for a parameter
	 */
	public void parseInteractive(T[] inputs, int off, BufferedReader input, Appendable output,
			String paramHelpIdentifier) {
		Map<ParameterData<T, Object>, Boolean> parametersCompleted = parse(inputs, off, output);

		outStream = output;

		for(Map.Entry<ParameterData<T, Object>, Boolean> paramComplete : parametersCompleted.entrySet()) {
			if(paramComplete.getValue() == false && paramComplete.getKey().isRequired()) {
				@SuppressWarnings("unchecked")
				ParameterData<String, ?> paramData = (ParameterData<String, ?>) paramComplete.getKey();

				ParameterParserResult parseRes = getParameterInteractive(paramData, input, output, paramHelpIdentifier);
				int i = 0;
				while(parseRes.isError() && parseRes.getParseError().getParseErrorType() == ParameterParserExceptionType.INVALID_PARSED_INPUT && i < ParameterSet.MAX_PARSE_ATTEMPTS) {
					try {
						output.append(parseRes.getParseError().getMessage())
							.append('\n');
					} catch (IOException e) {
						throw new RuntimeException("error writing parameter info and request to output stream", e);
					}

					parseRes = getParameterInteractive(paramData, input, output, paramHelpIdentifier);
					i++;
				}
				if(parseRes.isError()) {
					throw new RuntimeException(parseRes.getParseError().getMessage(), parseRes.getParseError().getCause());
				}
			}
		}

		outStream = null;
	}


	/** Create a help parameter which prints this parameter set's help information to {@code outStream}
	 * when its setter method is called.
	 * @param helpParamName the name of the help parameter
	 * @param helpMsg the help message to print
	 * @param helpParamAliases additional alias names of the help parameter
	 * @return the created help parameter
	 */
	private ParameterData<T, ?> createHelpParameter(String helpParamName, String helpMsg,
			String... helpParamAliases) {

		@SuppressWarnings("unchecked")
		ParameterData<T, Boolean> helpParam = (ParameterData<T, Boolean>)ParameterBuilder.newFlag()
				.setNameAndAliases(helpParamName, helpParamAliases)
				.setSetter((flag) -> {
					if(flag == false) {
						return;
					}
					try {
						outStream.append(helpMsg);
					} catch(Exception e) {
						throw new RuntimeException("writing parameter help message to output stream", e);
					}
				})
				.setHelpMessage("enter '" + helpParamName + "' to receive information about this program: ")
				.build();
		return helpParam;
	}


	private static final ParameterParserResult getParameterInteractive(ParameterData<String, ?> param,
			BufferedReader input, Appendable output, String paramHelpIdentifier) {
		try {
			output.append(param.getRequestParameterMessage());
		} catch (IOException e) {
			throw new RuntimeException("error writing parameter request to output stream", e);
		}

		ParameterParserResult parseRes = null;

		try {
			String line = input.readLine();
			while(paramHelpIdentifier != null && paramHelpIdentifier.equals(line)) {
				try {
					output.append(parameterInfo(param))
						.append('\n')
						.append(param.getRequestParameterMessage());
				} catch (IOException e) {
					throw new RuntimeException("error writing parameter info and request to output stream", e);
				}
				line = input.readLine();
			}
			List<String> inputs = new ArrayList<String>(2);
			inputs.add(param.getPrimaryName());
			// if the parsing is interactive (one line of input per parameter, use the entire line for non-array parameters)
			// this saves users having to quote every string parameter they enter
			if(param.isParameterArrayType()) {
				inputs = ParameterParser.parseParameters(line, '"', true, '\\', inputs);
			}
			else {
				inputs.add(line);
			}

			String[] inputsAry = inputs.toArray(new String[inputs.size()]);

			parseRes = param.parse(inputsAry, 0, inputs.size());

		} catch (IOException e) {
			throw new RuntimeException("error reading user parameter from input stream", e);
		}
		return parseRes;
	}


	/** Create a new parameter set with the given parameters and generate a help parameter
	 * @param parameters the list of parameters
	 * @param generateHelpParam true to generate a help parameter
	 * @param helpParamName the name of the help parameter
	 * @param helpParamAliases aliases for the name of the help parameter
	 * @return a {@link ParameterSet} that contains {@code parameters} and a help parameter containing
	 * information about all of the parameters
	 */
	@SafeVarargs
	public static final ParameterSet<String> newParameterSet(List<? extends ParameterData<String, ? extends Object>> parameters,
			boolean generateHelpParam, String helpParamName, String... helpParamAliases) {
		List<ParameterData<String, ? extends Object>> paramsCopy = new ArrayList<>(parameters);
		ParameterSet<String> paramSet = null;

		if(generateHelpParam == true && helpParamName != null) {
			List<String> aliases = helpParamAliases != null ? Arrays.asList(helpParamAliases) : null;

			StringBuilder sb = new StringBuilder("\t'" + helpParamName + "'" + orParamAliasesToString(",", aliases) +
					" - displays this help message\n");
			for(ParameterData<String, ?> param : parameters) {
				sb.append("\t" + parameterInfo(param) + "\n");
			}
			sb.append("\n");
			final String helpMsg = sb.toString();

			paramSet = new ParameterSet<String>(parameters, true, helpParamName, helpMsg, helpParamAliases);
		}
		else {
			paramSet = new ParameterSet<>(paramsCopy);
		}
		return paramSet;
	}


	private static final String parameterInfo(ParameterData<String, ?> param) {
		return parameterTypeToString(param) + " " + (param.isRequired() ? "(required) - " : "- ") +
				param.getHelpMessage();
	}


	/** Converts a list of aliases to a string in the format:
	 * {@code "prefix 'alias_1', 'alias_2', ..., 'alias_n'"}
	 * @param prefix the prefix to add to the beginning of the generated string
	 * @param aliases the list of aliases
	 * @return the prefix and list of aliases or {@code ""} if {@code aliases} is null or empty
	 */
	private static final String orParamAliasesToString(String prefix, List<String> aliases) {
		if(aliases != null && !aliases.isEmpty()) {
			StringBuilder sb = new StringBuilder(prefix != null ? prefix + " " : "");
			int size = aliases.size();
			for(int i = 0, count = size - 1; i < count; i++) {
				sb.append("'" + aliases.get(i) + "', ");
			}
			sb.append("'" + aliases.get(size - 1) + "'");
			return sb.toString();
		}
		return "";
	}


	/** Create a string representation of a parameter in the format:
	 * {@code 'parameter_name type', 'alias_1', ..., 'alias_n' [#IF_ENUM (one of: [enum_const_1, ..., enum_const_n])]}
	 * @param param the parameter to generate a string representation of
	 * @return the string representation of the parameter
	 */
	private static final <T> String parameterTypeToString(ParameterData<String, T> param) {
		String typeName = param.getParameterType() != ParameterType.FLAG ?
				param.getParameterType().name().toLowerCase() : "[false]";
		boolean isArray = param.isParameterArrayType();
		boolean isEnum = param.getParameterType() == ParameterType.ENUM;

		return "'" + param.getPrimaryName() + " " + (isArray ? typeName + " [" + typeName + " ...]" : typeName) +
				"'" + orParamAliasesToString(",", param.getAliases()) +
				(isEnum ? " (one of: " + param.getEnumMap().keySet().toString() + ")" : "");
	}

}
