package programParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Meta data about a program argument (e.g. a program's {@code main()} arguments)
 * @author TeamworkGuy2
 * @since 2014-11-18
 * @param <T>
 * @param <E>
 * @see ParameterData
 */
public class ParameterDataImpl<T extends CharSequence, E> implements ParameterData<T, E> {
	private static final CharSequence[] arrayOfTrue = new CharSequence[] { "true" };

	private ParameterType type;
	private boolean isArray;
	private Map<String, E> enumMap;
	private T primaryName;
	private List<T> aliases;
	private String helpMsg;
	private String requestParameterMsg;
	private boolean required;
	private Consumer<E> setter;
	private Predicate<E> validator;
	private Function<E, String> validatorMsgGenerator;


	public ParameterDataImpl(ParameterType type, boolean isArray, T primaryName, List<T> aliases,
			Consumer<E> setter, Predicate<E> validator, Function<E, String> validatorMessageGenerator,
			String helpMsg, String requestParameterMsg, boolean required) {
		super();
		if(type == ParameterType.ENUM) {
			throw new IllegalArgumentException("cannot create parameter of type '" + type + "'" +
					" without an enumeration of the enum's elements");
		}
		this.type = type;
		this.isArray = isArray;
		this.primaryName = primaryName;
		this.aliases = aliases;
		this.setter = setter;
		this.validator = validator;
		this.validatorMsgGenerator = validatorMessageGenerator;
		this.helpMsg = helpMsg;
		this.requestParameterMsg = requestParameterMsg;
		this.required = required;
	}


	public ParameterDataImpl(ParameterType type, boolean isArray, Map<String, E> enumMap,
			T primaryName, List<T> aliases, Consumer<E> setter, Predicate<E> validator, Function<E, String> validatorMessageGenerator,
			String helpMsg, String requestParameterMsg, boolean required) {
		super();
		this.type = type;
		this.isArray = isArray;
		this.enumMap = enumMap;
		this.primaryName = primaryName;
		this.aliases = aliases;
		this.setter = setter;
		this.validator = validator;
		this.validatorMsgGenerator = validatorMessageGenerator;
		this.helpMsg = helpMsg;
		this.requestParameterMsg = requestParameterMsg;
		this.required = required;
	}


	@Override
	public ParameterType getParameterType() {
		return type;
	}


	@Override
	public boolean isParameterArrayType() {
		return isArray;
	}


	@Override
	public Map<String, E> getEnumMap() {
		return enumMap;
	}


	@Override
	public T getPrimaryName() {
		return primaryName;
	}


	@Override
	public List<T> getAliases() {
		return aliases;
	}


	@Override
	public String getHelpMessage() {
		return helpMsg;
	}


	@Override
	public String getRequestParameterMessage() {
		return requestParameterMsg;
	}


	@Override
	public boolean isRequired() {
		return required;
	}


	@Override
	public Consumer<E> getSetter() {
		return setter;
	}


	@Override
	public Predicate<E> getValidator() {
		return validator;
	}


	@Override
	public Function<E, String> getValidatorMessageGenerator() {
		return validatorMsgGenerator;
	}


	@Override
	public ParameterParserResult parse(T[] strings) {
		return parse(strings, 0, strings.length);
	}


	@Override
	public ParameterParserResult parse(T[] strings, int off, int len) {
		validateParameterName(strings[off]);
		off++;
		len--;
		// inject true for flags that exist but do not have true after them
		if(len == 0 && type == ParameterType.FLAG) {
			@SuppressWarnings("unchecked")
			T[] trueStrAry = (T[])arrayOfTrue;
			strings = trueStrAry;
			off = 0;
			len = 1;
		}
		checkInputArray(type, isArray, strings, off, len);

		ParameterParserResult result = null;

		switch(type) {
		case ENUM:
			result = readEnum(primaryName.toString(), isArray, enumMap, strings, off, len, setter, validator, validatorMsgGenerator);
			break;
		case FLAG:
			{
				@SuppressWarnings("unchecked")
				Consumer<Boolean> setFunc = (Consumer<Boolean>)setter;
				@SuppressWarnings("unchecked")
				Predicate<Boolean> validateFunc = (Predicate<Boolean>)validator;
				@SuppressWarnings("unchecked")
				Function<Boolean, String> validateMsgGenFunc = (Function<Boolean, String>)validatorMsgGenerator;
				result = readFlag(primaryName.toString(), isArray, strings, off, len, setFunc, validateFunc, validateMsgGenFunc);
			}
			break;
		case FLOAT:
			{
				@SuppressWarnings("unchecked")
				Consumer<Float> setFunc = (Consumer<Float>)setter;
				@SuppressWarnings("unchecked")
				Predicate<Float> validateFunc = (Predicate<Float>)validator;
				@SuppressWarnings("unchecked")
				Function<Float, String> validateMsgGenFunc = (Function<Float, String>)validatorMsgGenerator;
				result = readFloat(primaryName.toString(), isArray, strings, off, len, setFunc, validateFunc, validateMsgGenFunc);
			}
			break;
		case INTEGER:
			{
				@SuppressWarnings("unchecked")
				Consumer<Integer> setFunc = (Consumer<Integer>)setter;
				@SuppressWarnings("unchecked")
				Predicate<Integer> validateFunc = (Predicate<Integer>)validator;
				@SuppressWarnings("unchecked")
				Function<Integer, String> validateMsgGenFunc = (Function<Integer, String>)validatorMsgGenerator;
				result = readInteger(primaryName.toString(), isArray, strings, off, len, setFunc, validateFunc, validateMsgGenFunc);
			}
			break;
		case PATH:
			{
				@SuppressWarnings("unchecked")
				Consumer<Path> setFunc = (Consumer<Path>)setter;
				@SuppressWarnings("unchecked")
				Predicate<Path> validateFunc = (Predicate<Path>)validator;
				@SuppressWarnings("unchecked")
				Function<Path, String> validateMsgGenFunc = (Function<Path, String>)validatorMsgGenerator;
				result = readPath(primaryName.toString(), isArray, strings, off, len, setFunc, validateFunc, validateMsgGenFunc);
			}
			break;
		case TEXT:
			{
				@SuppressWarnings("unchecked")
				Consumer<String> setFunc = (Consumer<String>)setter;
				@SuppressWarnings("unchecked")
				Predicate<String> validateFunc = (Predicate<String>)validator;
				@SuppressWarnings("unchecked")
				Function<String, String> validateMsgGenFunc = (Function<String, String>)validatorMsgGenerator;
				result = readText(primaryName.toString(), isArray, strings, off, len, setFunc, validateFunc, validateMsgGenFunc);
			}
			break;
		default:
			throw new IllegalStateException("unknown " + ParameterType.class + " enum constant '" + type + "'");
		}
		return result;
	}


	private final void validateParameterName(T inputName) {
		if(!isParameterName(inputName)) {
			throw new IllegalArgumentException("unknown argument name '" + inputName + "'");
		}
	}


	private static final void checkInputArray(ParameterType type, boolean isArray, Object[] ary, int off, int len) {
		if(isArray) {
			if(ary.length - off < 0) {
				throw new IllegalArgumentException("expected zero or more arguments of type " + type);
			}
		}
		else {
			if(len != 1) {
				throw new IllegalArgumentException("expected one argument of type " + type);
			}
		}
	}


	private static final <T> ParameterParserResult unknownInputArg(String parameterName, ParameterType type, T arg) {
		RuntimeException errMsg = new IllegalArgumentException("parameter: " + parameterName + ", unkown argument '" + arg + "', expected type " + type);
		return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.INPUT_PARSING_EXCEPTION, errMsg.getMessage(), errMsg));
	}


	private static final <T> ParameterParserResult unknownInputArg(String parameterName, ParameterType type, T arg, Exception cause) {
		RuntimeException errMsg = new IllegalArgumentException("parameter: " + parameterName + ", unkown program argument '" + arg + "', expected type " + type +
				", caused by exception: " + cause, cause);
		return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.INPUT_PARSING_EXCEPTION, errMsg.getMessage(), errMsg));
	}


	private static final <T, R> RuntimeException setterException(String parameterName, ParameterType type, Consumer<T> setter, R value, Exception cause) {
		return new RuntimeException("parameter: " + parameterName + " (" + type.name() + ")" +
				", error calling setter function (" + setter.getClass().getTypeName() + "<" + Arrays.toString(setter.getClass().getTypeParameters()) + ">) " + setter +
				" with argument '" + (value.getClass().isArray() ? Arrays.deepToString(new Object[] { value }) : value) + "' (" + value.getClass().getCanonicalName() + ")", cause);
	}


	private static final <T, R> String invalidParseData(String parameterName, ParameterType type, Function<R, String> validatorMessageGenerator, R value) {
		String message = validatorMessageGenerator != null
				? validatorMessageGenerator.apply(value)
				: "invalid " + type + " '" + parameterName + "' value of '" + value + "'";
		return message;
	}


	/**
	 * @return null if no error occurred
	 */
	private static final <E> ParameterParserResult trySetValue(ParameterType type, E value, Consumer<E> setter,
			Predicate<E> validator, Function<E, String> validatorMessageGenerator, String parameterName) {
		if(validator != null) {
			try {
				if(!validator.test(value)) {
					String message = null;
					try {
						message = invalidParseData(parameterName, type, validatorMessageGenerator, value);
					} catch(Exception e) {
						return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.VALIDATOR_MESSAGE_GENERATOR_EXCEPTION, "validator message generator for " + type + " threw an exception", null));
					}
					return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.INVALID_PARSED_INPUT, message, new IllegalArgumentException()));
				}
			} catch(Exception e) {
				return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.VALIDATOR_EXCEPTION, "validator for " + type + " threw an exception", e));
			}
		}

		try {
			setter.accept(value);
		} catch (Exception e) {
			RuntimeException cause = setterException(parameterName, ParameterType.FLAG, setter, value, e);
			return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.SETTER_EXCEPTION, cause.getMessage(), cause));
		}
		return null;
	}


	/**
	 * @return null if no error occurred
	 */
	private static final <E, R> ParameterParserResult trySetValues(ParameterType type, R values, Consumer<E> setter,
			Predicate<E> validator, Function<E, String> validatorMessageGenerator, String parameterName) {
		if(validator != null) {
			Object[] valueAry = (Object[])values;
			int i = 0;
			int size = valueAry.length;
			try {
				for(i = 0; i < size; i++) {
					@SuppressWarnings("unchecked")
					E val = (E)valueAry[i];
					if(!validator.test(val)) {
						String message = null;
						try {
							message = invalidParseData(parameterName, type, validatorMessageGenerator, val);
						} catch(Exception e) {
							return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.VALIDATOR_MESSAGE_GENERATOR_EXCEPTION, "validator message generator for parameter " + (i + 1) + " of " + type + " threw an exception", null));
						}
						return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.INVALID_PARSED_INPUT, message, new IllegalArgumentException()));
					}
				}
			} catch(Exception e) {
				return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.VALIDATOR_EXCEPTION, "validator for parameter " + (i + 1) + " of " + type + " threw an exception", e));
			}
		}

		try {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Consumer<R> arraySetter = (Consumer<R>)(Consumer)setter;
			arraySetter.accept(values);
		} catch (Exception e) {
			RuntimeException cause = setterException(parameterName, ParameterType.FLAG, setter, values, e);
			return new ParameterParserResult(new ParameterParserException(ParameterParserExceptionType.SETTER_EXCEPTION, cause.getMessage(), cause));
		}
		return null;
	}


	private static final <E, T extends CharSequence> ParameterParserResult readEnum(String parameterName, boolean isArray, Map<String, E> enumMap,
			T[] strs, int off, int len, Consumer<E> setter, Predicate<E> validator, Function<E, String> validatorMessageGenerator) {
		E[] enumVals = null;
		if(isArray) {
			@SuppressWarnings("unchecked")
			E[] vals = (E[]) new Object[len];
			enumVals = vals;
		}

		ParameterType paramType = ParameterType.ENUM;
		ParameterParserResult result = null;

		List<E> enumResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			E enumVal = enumMap.get(strs[i].toString());
			if(enumVal == null) {
				return unknownInputArg(parameterName, paramType, strs[i]);
			}
			if(!isArray) {
				result = trySetValue(paramType, enumVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					enumResults.add(enumVal);
				}
				else {
					return result;
				}
			}
			else {
				enumVals[i - off] = enumVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, enumVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, enumVals, true);
			}
		}
		else {
			boolean hasOneResVal = enumResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, enumResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, enumResults.toArray(), true);
			}
		}
		return result;
	}


	private static final <T extends CharSequence> ParameterParserResult readFlag(String parameterName, boolean isArray, T[] strs, int off, int len,
			Consumer<Boolean> setter, Predicate<Boolean> validator, Function<Boolean, String> validatorMessageGenerator) {
		boolean[] flagVals = null;
		if(isArray) {
			flagVals = new boolean[len];
		}

		ParameterType paramType = ParameterType.FLAG;
		ParameterParserResult result = null;

		List<Boolean> flagResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			String str = strs[i].toString();
			boolean flagVal = false;
			if("true".equals(str) || "false".equals(str)) {
				flagVal = "true".equals(str);
			}
			else {
				return unknownInputArg(parameterName, paramType, strs[i]);
			}
			if(!isArray) {
				result = trySetValue(paramType, flagVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					flagResults.add(flagVal);
				}
				else {
					return result;
				}
			}
			else {
				flagVals[i - off] = flagVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, flagVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, flagVals, true);
			}
		}
		else {
			boolean hasOneResVal = flagResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, flagResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, flagResults.toArray(), true);
			}
		}
		return result;
	}


	private static final <T extends CharSequence> ParameterParserResult readFloat(String parameterName, boolean isArray, T[] strs, int off, int len,
			Consumer<Float> setter, Predicate<Float> validator, Function<Float, String> validatorMessageGenerator) {
		float[] floatVals = null;
		if(isArray) {
			floatVals = new float[len];
		}

		ParameterType paramType = ParameterType.FLOAT;
		ParameterParserResult result = null;

		List<Float> floatResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			float floatVal = 0;
			try {
				floatVal = Float.parseFloat(strs[i].toString());
			} catch(Exception e) {
				return unknownInputArg(parameterName, paramType, strs[i], e);
			}
			if(!isArray) {
				result = trySetValue(paramType, floatVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					floatResults.add(floatVal);
				}
				else {
					return result;
				}
			}
			else {
				floatVals[i - off] = floatVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, floatVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, floatVals, true);
			}
		}
		else {
			boolean hasOneResVal = floatResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, floatResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, floatResults.toArray(), true);
			}
		}
		return result;
	}


	private static final <T extends CharSequence> ParameterParserResult readInteger(String parameterName, boolean isArray, T[] strs, int off, int len,
			Consumer<Integer> setter, Predicate<Integer> validator, Function<Integer, String> validatorMessageGenerator) {
		int[] intVals = null;
		if(isArray) {
			intVals = new int[len];
		}

		ParameterType paramType = ParameterType.INTEGER;
		ParameterParserResult result = null;

		List<Integer> intResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			int intVal = 0;
			try {
				intVal = Integer.parseInt(strs[i].toString());
			} catch(Exception e) {
				return unknownInputArg(parameterName, paramType, strs[i], e);
			}
			if(!isArray) {
				result = trySetValue(paramType, intVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					intResults.add(intVal);
				}
				else {
					return result;
				}
			}
			else {
				intVals[i - off] = intVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, intVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, intVals, true);
			}
		}
		else {
			boolean hasOneResVal = intResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, intResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, intResults.toArray(), true);
			}
		}
		return result;
	}


	private static final <T extends CharSequence> ParameterParserResult readPath(String parameterName, boolean isArray, T[] strs, int off, int len,
			Consumer<Path> setter, Predicate<Path> validator, Function<Path, String> validatorMessageGenerator) {
		Path[] pathVals = null;
		if(isArray) {
			pathVals = new Path[len];
		}

		ParameterType paramType = ParameterType.PATH;
		ParameterParserResult result = null;

		List<Path> pathResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			Path pathVal = null;
			try {
				pathVal = Paths.get(strs[i].toString());
			} catch(Exception e) {
				return unknownInputArg(parameterName, paramType, strs[i], e);
			}
			if(!isArray) {
				result = trySetValue(paramType, pathVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					pathResults.add(pathVal);
				}
				else {
					return result;
				}
			}
			else {
				pathVals[i - off] = pathVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, pathVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, pathVals, true);
			}
		}
		else {
			boolean hasOneResVal = pathResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, pathResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, pathResults.toArray(), true);
			}
		}
		return result;
	}


	private static final <T extends CharSequence> ParameterParserResult readText(String parameterName, boolean isArray, T[] strs, int off, int len,
			Consumer<String> setter, Predicate<String> validator, Function<String, String> validatorMessageGenerator) {
		String[] textVals = null;
		if(isArray) {
			textVals = new String[len];
		}

		ParameterType paramType = ParameterType.TEXT;
		ParameterParserResult result = null;

		List<String> textResults = new ArrayList<>();
		for(int i = off, size = off + len; i < size; i++) {
			String textVal = null;
			try {
				textVal = strs[i].toString();
			} catch(Exception e) {
				return unknownInputArg(parameterName, paramType, strs[i], e);
			}
			if(!isArray) {
				result = trySetValue(paramType, textVal, setter, validator, validatorMessageGenerator, parameterName);
				if(result == null) {
					textResults.add(textVal);
				}
				else {
					return result;
				}
			}
			else {
				textVals[i - off] = textVal;
			}
		}

		if(isArray) {
			result = trySetValues(paramType, textVals, setter, validator, validatorMessageGenerator, parameterName);
			if(result == null) {
				result = new ParameterParserResult(paramType, textVals, true);
			}
		}
		else {
			boolean hasOneResVal = textResults.size() == 1;
			if(hasOneResVal) {
				result = new ParameterParserResult(paramType, textResults.get(0), false);
			}
			else {
				result = new ParameterParserResult(paramType, textResults.toArray(), true);
			}
		}
		return result;
	}

}
