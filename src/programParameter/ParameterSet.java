package programParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A set of {@link ParameterMetaData} instances to parse against a give set of input values
 * 
 * @param <T> the parameter's name type
 * 
 * @author TeamworkGuy2
 * @since 2014-11-22
 */
public final class ParameterSet<T extends String> {
	private List<ParameterMetaData<T, ?>> parameters;
	/** a map of all parameter names and aliases to their parameters */
	private Map<T, ParameterMetaData<T, ?>> parameterNameMap;
	/** @param out the output stream to print the help message to when it is parsed by {@link ParameterSet#parse()} */
	private Appendable outStream;


	public ParameterSet(List<ParameterMetaData<T, ?>> parameters) {
		this(parameters, false, null, null, (String[])null);
	}


	/** Create a parameter set from a list of parameters
	 * @param parameters the list of parameters to store in this parameter set
	 */
	public ParameterSet(List<ParameterMetaData<T, ?>> parameters,
			boolean buildHelpParam, String helpParamName, String helpMsg, String... helpParamAliases) {
		this.parameters = new ArrayList<>();
		this.parameters.addAll(parameters);

		if(buildHelpParam) {
			this.parameters.add(createHelpParameter(helpParamName, helpMsg, helpParamAliases));
		}

		this.parameterNameMap = new HashMap<>();

		for(ParameterMetaData<T, ?> parameter : this.parameters) {
			this.parameterNameMap.put(parameter.getPrimaryName(), parameter);
			for(T name : parameter.getAliases()) {
				this.parameterNameMap.put(name, parameter);
			}
		}
	}


	/** Parse an array of inputs and invoke {@link ParameterMetaData#parse()} on
	 * the parameters in this parameter set that match any of the inputs.
	 * @param inputs the array of inputs to parse
	 * @param off the offset into {@code inputs} at which to start parsing
	 */
	public Map<ParameterMetaData<T, ?>, Boolean> parse(T[] inputs, int off, Appendable output) {
		outStream = output;
		ParameterMetaData<T, ?> param = null;
		ParameterMetaData<T, ?> paramTemp = null;
		int paramStart = -1;
		for(int i = off, size = inputs.length; i < size; i++) {
			paramTemp = parameterNameMap.get(inputs[i]);
			if(paramTemp != null) {
				param = paramTemp;
				paramStart = i;
				if(paramStart+1 >= inputs.length) {
					param.parse(inputs, paramStart, 1);
				}
				break;
			}
		}

		Map<ParameterMetaData<T, ?>, Boolean> parametersCompleted = new HashMap<>();
		for(ParameterMetaData<T, ?> parameter : parameters) {
			parametersCompleted.put(parameter, false);
		}
		int nextParamStart = -1;
		for(int i = paramStart+1, size = inputs.length; i < size; i++) {
			paramTemp = parameterNameMap.get(inputs[i]);
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


	/** Parse an array of inputs and invoke {@link ParameterMetaData#parse()} on
	 * the parameters in this parameter set that match any of the inputs.
	 * This differs from {@link #parse(Object[], int)} because missing parameters
	 * are requested using the specified output stream and parsed from the specified input stream
	 * @param inputs the array of inputs to parse
	 * @param off the offset into {@code inputs} at which to start parsing
	 * @param input the input stream to read user input from
	 * @param output the output stream to print user information and prompts to
	 * @param paramHelpIdentifier the name of the command that causes help information
	 * to be printed for a parameter
	 */
	public void parseInteractive(T[] inputs, BufferedReader input, Appendable output,
			String paramHelpIdentifier) {
		parseInteractive(inputs, 0, input, output, paramHelpIdentifier);
	}


	/** Parse an array of inputs and invoke {@link ParameterMetaData#parse()} on
	 * the parameters in this parameter set that match any of the inputs.
	 * This differs from {@link #parse(Object[], int)} because missing parameters
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
		Map<ParameterMetaData<T, ?>, Boolean> parametersCompleted = parse(inputs, off, output);

		outStream = output;

		for(Map.Entry<ParameterMetaData<T, ?>, Boolean> paramComplete : parametersCompleted.entrySet()) {
			if(paramComplete.getValue() == false && paramComplete.getKey().isRequired()) {
				@SuppressWarnings("unchecked")
				ParameterMetaData<String, ?> paramData = (ParameterMetaData<String, ?>) paramComplete.getKey();
				getParameterInteractive(paramData, input, output, paramHelpIdentifier);
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
	private ParameterMetaData<T, ?> createHelpParameter(String helpParamName, String helpMsg,
			String... helpParamAliases) {
		List<String> aliases = helpParamAliases != null ? Arrays.asList(helpParamAliases) : null;

		@SuppressWarnings("unchecked")
		ParameterMetaData<T, Boolean> helpParam = (ParameterMetaData<T, Boolean>) new ParameterMetaDataImpl<String, Boolean>(
				ParameterType.FLAG, false,
				helpParamName, aliases, (flag) -> {
					if(flag == false) {
						return;
					}
					try {
						outStream.append(helpMsg);
					} catch(Exception e) {
						throw new RuntimeException("writing parameter help message to output stream", e);
					}
				}, helpMsg, "enter '" + helpParamName + "' to receive information about this program: ", false);

		return helpParam;
	}


	private static final void getParameterInteractive(ParameterMetaData<String, ?> param,
			BufferedReader input, Appendable output, String paramHelpIdentifier) {
		try {
			output.append(param.getRequestParameterMessage());
		} catch (IOException e) {
			throw new RuntimeException("error writing parameter request to output stream", e);
		}
		try {
			String line = input.readLine();
			while(paramHelpIdentifier != null && paramHelpIdentifier.equals(line)) {
				try {
					output.append(parameterInfo(param));
					output.append('\n');
					output.append(param.getRequestParameterMessage());
				} catch (IOException e) {
					throw new RuntimeException("error writing parameter info and request to output stream", e);
				}
				line = input.readLine();
			}
			List<String> inputs = new ArrayList<String>();
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
			param.parse(inputsAry, 0, inputs.size());
		} catch (IOException e) {
			throw new RuntimeException("error reading user parameter from input stream", e);
		}
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
	public static final ParameterSet<String> newParameterSet(List<ParameterMetaData<String, ?>> parameters,
			boolean generateHelpParam, String helpParamName, String... helpParamAliases) {
		List<ParameterMetaData<String, ?>> paramsCopy = new ArrayList<>(parameters);
		ParameterSet<String> paramSet = null;

		if(generateHelpParam == true && helpParamName != null) {
			List<String> aliases = helpParamAliases != null ? Arrays.asList(helpParamAliases) : null;

			StringBuilder strB = new StringBuilder("\t'" + helpParamName + "'" + orParamAliasesToString(",", aliases) +
					" - displays this help message\n");
			for(ParameterMetaData<String, ?> param : parameters) {
				strB.append("\t" + parameterInfo(param) + "\n");
			}
			strB.append("\n");
			final String helpMsg = strB.toString();

			paramSet = new ParameterSet<String>(parameters, true, helpParamName, helpMsg, helpParamAliases);
		}
		else {
			paramSet = new ParameterSet<>(paramsCopy);
		}
		return paramSet;
	}


	private static final String parameterInfo(ParameterMetaData<String, ?> param) {
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
			StringBuilder strB = new StringBuilder(prefix != null ? prefix + " " : "");
			int size = aliases.size();
			for(int i = 0, count = size - 1; i < count; i++) {
				strB.append("'" + aliases.get(i) + "', ");
			}
			strB.append("'" + aliases.get(size - 1) + "'");
			return strB.toString();
		}
		return "";
	}


	/** Create a string representation of a parameter in the format:
	 * {@code 'parameter_name type', 'alias_1', ..., 'alias_n' [#IF_ENUM (one of: [enum_const_1, ..., enum_const_n])]}
	 * @param param the parameter to generate a string representation of
	 * @return the string representation of the parameter
	 */
	private static final String parameterTypeToString(ParameterMetaData<String, ?> param) {
		String typeName = param.getParameterType() != ParameterType.FLAG ?
				param.getParameterType().name().toLowerCase() : "[false]";
		boolean isArray = param.isParameterArrayType();
		boolean isEnum = param.getParameterType() == ParameterType.ENUM;
		return "'" + param.getPrimaryName() + " " + (isArray ? typeName + " [" + typeName + " ...]" : typeName) +
				"'" + orParamAliasesToString(",", param.getAliases()) +
				(isEnum ? " (one of: " + param.getEnumMap().keySet().toString() + ")" : "");
	}

}
