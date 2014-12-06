package programParameter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import functionUtils.TriConsumer;

/** A program parameter that can be identified by a list of aliases
 * and can be executed.
 * @param <U> an extra data object that will be passed through to each
 * parameter that is identified and called
 * by this class
 * @author TeamworkGuy2
 * @since 2014-5-18
 */
public final class ParameterParser<U> {
	private Map<String, BiConsumer<Supplier<String>, U>> parameterMap;
	private List<Map.Entry<Pattern, TriConsumer<Supplier<String>, MatchResult, U>>> regexParameters;


	public ParameterParser() {
		this.parameterMap = new HashMap<>();
		this.regexParameters = new ArrayList<>();
	}


	public <T extends Enum<?> & ParameterAliases<String> & BiConsumer<Supplier<String>, U>> ParameterParser(
			Class<T> enumParameterType) {
		this();
		// Fill the parameter map with strings identifying the various parameters
		T[] enums = enumParameterType.getEnumConstants();
		for(T parameter : enums) {
			for(String paramName : parameter.getAliases()) {
				parameterMap.put(paramName, parameter);
			}
		}
	}


	public void addParameter(String alias, BiConsumer<Supplier<String>, U> param) {
		this.parameterMap.put(alias, param);
	}


	public void addParameter(List<String> aliases, BiConsumer<Supplier<String>, U> param) {
		for(int i = 0, size = aliases.size(); i < size; i++) {
			this.parameterMap.put(aliases.get(i), param);
		}
	}


	public void addRegexParameter(String regex, TriConsumer<Supplier<String>, MatchResult, U> param) {
		Pattern pattern = Pattern.compile(regex);
		this.regexParameters.add(new AbstractMap.SimpleImmutableEntry<>(pattern, param));
	}


	/** Search through the list of supplied strings for parameter aliases
	 * and call the associated parameters
	 * @param parameters a supplier of parsed parameter strings
	 * @param data program specific data to pass along to the parameter
	 * @return the number of parameters successfully found and called from
	 * the string supplier
	 */
	public int callParameters(Supplier<String> parameters, U data) {
		String paramName = parameters.get();
		int paramsCalled = 0;
		while(paramName != null) {
			if(findAndCallParameter(paramName, parameters, data)) { paramsCalled++; }
			paramName = parameters.get();
		}
		return paramsCalled;
	}


	/** Search through the list of supplied strings for parameter aliases
	 * and call the associated parameters
	 * @param parameterName a possible parameter strings name
	 * @return the parameter found that matches the parameter name specified,
	 * or null if a matching parameter could not be found
	 */
	/*public BiConsumer<Supplier<String>, U> getParameter(String paramName) {
		BiConsumer<Supplier<String>, U> param = findParameter(paramName);
		return param;
	}*/


	private final boolean findAndCallParameter(String paramName, Supplier<String> remainingParameters, U data) {
		// Check for matching parameter aliases
		BiConsumer<Supplier<String>, U> aliasParam = parameterMap.get(paramName);
		if(aliasParam != null) {
			aliasParam.accept(remainingParameters, data);
			return true;
		}
		// Check for matching regex aliases
		Map.Entry<Pattern, TriConsumer<Supplier<String>, MatchResult, U>> regexParam = null;
		for(int i = 0, size = regexParameters.size(); i < size; i++) {
			Matcher match = regexParameters.get(i).getKey().matcher(paramName);
			if(match.find()) {
				regexParam = regexParameters.get(i);
				regexParam.getValue().accept(remainingParameters, match, data);
				return true;
			}
		}

		return false;
	}


	/** Parse a string into an array of strings by splitting at spaces and
	 * quotes. Primarily used to parse a single parameter string into multiple parameters.
	 * This method calls is the same as {@code parseParameters(param, '"')}.
	 * @param param the string to split, this string is trimmed first.
	 * @return a modifiable list of strings containing the original string split at
	 * spaces or quotes. Quotes without spaces before them are not split.
	 * Spaces between opening and closing quotes are not split.
	 */
	public static final List<String> parseParameters(String param) {
		return parseParameters(param, '"');
	}


	/** Parse a string into an array of strings by splitting at spaces and
	 * quote characters. Primarily used to parse a single parameter string
	 * into multiple parameters.
	 * @param param the string to split, this string is trimmed first.
	 * @param quote the character that represents a quote (normally a {@code "} or {@code '}).
	 * @return a modifiable list of strings containing the original string split at
	 * spaces or quote characters. Quote characters without spaces before them are not split.
	 * Spaces between opening and closing quote characters are not split.
	 */
	public static final List<String> parseParameters(String param, final char quote) {
		// used for debugging
		//char[] spaces = new char[800];
		//for(int i = 0; i < spaces.length; i++) { spaces[i] = ' '; }

		param = param.trim();
		List<String> params = new ArrayList<String>();
		boolean lookingForQuote = false;
		boolean lookingForWhitespace = false;
		boolean finishedQuotes = false;
		int subsequenceStartIndex = -1;
		//PrintStream outP = System.out;
		//char[] spaces = new String("                                                                ").toCharArray();
		if(param.charAt(0) == quote) {
			lookingForQuote = true;
			subsequenceStartIndex = 0;
		}
		else {
			lookingForWhitespace = true;
			subsequenceStartIndex = 0;
		}

		// For each character in the string
		for(int i = 0, size = param.length(); i < size; i++) {
			char c = param.charAt(i);
			// Search for a closing quote
			if(i != 0 && lookingForQuote && c == quote) {
				//outP.println(param);
				//outP.println(new String(spaces, 0, i) + "^end quote");
				lookingForQuote = false;
				finishedQuotes = true;
				// skip to the next character upon finding a closing quote
				continue;
			}
			// Search for an opening quote
			if(c == quote) {
				//outP.println(param);
				//outP.println(new String(spaces, 0, i) + "^start quote");
				lookingForQuote = true;
				if(lookingForWhitespace) {
					lookingForWhitespace = false;
				}
				else {
					subsequenceStartIndex = i;
				}
			}
			// Search for closing (second) space while not between an opening and closing quote
			if(!lookingForQuote && lookingForWhitespace && Character.isWhitespace(c)) {
				//outP.println(param);
				//outP.println(new String(spaces, 0, i) + "^end space");
				params.add(param.substring(subsequenceStartIndex, i));
				lookingForWhitespace = false;
				subsequenceStartIndex = -1;
				//continue;
			}
			// Search for a starting (first) space while not between an opening and closing quote
			if(!lookingForQuote && Character.isWhitespace(c)) {
				//outP.println(param);
				//outP.println(new String(spaces, 0, i) + "^start space");
				lookingForWhitespace = true;
				if(finishedQuotes) {
					finishedQuotes = false;
					params.add(trimQuotes(param.substring(subsequenceStartIndex, i)));
					//startIndex = -1;
				}
				subsequenceStartIndex = i+1;
			}
		}

		if(subsequenceStartIndex != -1) {
			//outP.println(param);
			//outP.println(new String(spaces, 0, param.length()-1) + "^end");
			params.add(trimQuotes(param.substring(subsequenceStartIndex, param.length())));
			subsequenceStartIndex = -1;
		}
		else if(params.size() == 0) {
			params.add(param.substring(0, param.length()));
			subsequenceStartIndex = -1;
		}
		return params;
	}


	/** Trim quotes from the start and end of a string if there
	 * are is a quote {@code "} at both the beginning and end of the string.
	 * @param strs the list of strings to trim quotes from, the
	 * modified strings are replaced in the list by their trimmed versions.
	 * This list must be modifiable, its size will not be changed.
	 */
	public static final void trimQuotes(List<String> strs) {
		for(int i = 0, size = strs.size(); i < size; i++) {
			String str = strs.get(i);
			if(str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length()-1) == '"') {
				strs.set(i, str.substring(1, str.length()-1));
			}
		}
	}


	public static final String trimQuotes(String str) {
		if(str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length()-1) == '"') {
			return str.substring(1, str.length()-1);
		}
		return str;
	}

}
