package programParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Meta data about a program argument (e.g. a program's {@code main()} arguments)
 * @author TeamworkGuy2
 * @since 2014-11-18
 * @param <T>
 * @param <O>
 * @param <E>
 * @see ParameterMetaData
 */
public class ParameterMetaDataImpl<T extends CharSequence, E> implements ParameterMetaData<T, E> {
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


	public ParameterMetaDataImpl(ParameterType type, boolean isArray, T primaryName, List<T> aliases,
			Consumer<E> setter, String helpMsg, String requestParameterMsg, boolean required) {
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
		this.helpMsg = helpMsg;
		this.requestParameterMsg = requestParameterMsg;
		this.required = required;
	}


	public ParameterMetaDataImpl(ParameterType type, boolean isArray, Map<String, E> enumMap,
			T primaryName, List<T> aliases, Consumer<E> setter, String helpMsg, String requestParameterMsg, boolean required) {
		super();
		this.type = type;
		this.isArray = isArray;
		this.enumMap = enumMap;
		this.primaryName = primaryName;
		this.aliases = aliases;
		this.setter = setter;
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
	public void parse(T[] strings) {
		parse(strings, 0, strings.length);
	}


	@Override
	public void parse(T[] strings, int off, int len) {
		validateParameterName(strings[off]);
		off++;
		len--;
		// inject true for flags that exist but do not have true after them
		if(len == 0 && type == ParameterType.FLAG) {
			strings = (T[])arrayOfTrue;
			off = 0;
			len = 1;
		}
		checkInputArray(type, isArray, strings, off, len);

		switch(type) {
		case ENUM:
			readEnum(isArray, enumMap, strings, off, len, setter);
			break;
		case FLAG:
			readFlag(isArray, strings, off, len, (Consumer<Boolean>) setter);
			break;
		case FLOAT:
			readFloat(isArray, strings, off, len, (Consumer<Float>) setter);
			break;
		case INTEGER:
			readInteger(isArray, strings, off, len, (Consumer<Integer>) setter);
			break;
		case PATH:
			readPath(isArray, strings, off, len, (Consumer<Path>) setter);
			break;
		case TEXT:
			readText(isArray, strings, off, len, (Consumer<String>) setter);
			break;
		default:
			throw new IllegalStateException("unknown " + ParameterType.class + " enum constant '" + type + "'");
		}
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


	private static final <T> void unknownInputArg(ParameterType type, T arg) {
		throw new IllegalArgumentException("unkown argument '" + arg + "', expected type " + type);
	}


	private static final <T> void unknownInputArg(ParameterType type, T arg, Exception cause) {
		throw new IllegalArgumentException("unkown program argument '" + arg + "', expected type " + type +
				", caused by exception: " + cause);
	}


	private static final <T> void setterException(Consumer<T> setter, T value, Exception cause) {
		throw new RuntimeException("error calling setter function " + setter + " with argument '" + value + "'", cause);
	}


	private static final <E, T extends CharSequence> void readEnum(boolean isArray, Map<String, E> enumMap,
			T[] strs, int off, int len, Consumer<E> setter) {
		E[] enumVals = null;
		if(isArray) {
			@SuppressWarnings("unchecked")
			E[] vals = (E[]) new Object[len];
			enumVals = vals;
		}

		for(int i = off, size = off + len; i < size; i++) {
			E enumVal = enumMap.get(strs[i].toString());
			if(enumVal == null) {
				unknownInputArg(ParameterType.ENUM, strs[i]);
			}
			if(!isArray) {
				try {
					setter.accept(enumVal);
				} catch (Exception e) {
					setterException(setter, enumVal, e);
				}
			}
			else {
				enumVals[i - off] = enumVal;
			}
		}
		if(isArray) {
			((Consumer<E[]>)(Consumer)setter).accept(enumVals);
		}
	}


	private static final <T extends CharSequence> void readFlag(boolean isArray, T[] strs, int off, int len,
			Consumer<Boolean> setter) {
		boolean[] flagVals = null;
		if(isArray) {
			flagVals = new boolean[len];
		}

		for(int i = off, size = off + len; i < size; i++) {
			String str = strs[i].toString();
			boolean flagVal = false;
			if("true".equals(str) || "false".equals(str)) {
				flagVal = "true".equals(str);
			}
			else {
				unknownInputArg(ParameterType.FLAG, strs[i]);
			}
			if(!isArray) {
				try {
					setter.accept(flagVal);
				} catch (Exception e) {
					setterException(setter, flagVal, e);
				}
			}
			else {
				flagVals[i - off] = flagVal;
			}
		}
		if(isArray) {
			((Consumer<boolean[]>)(Consumer)setter).accept(flagVals);
		}
	}


	private static final <T extends CharSequence> void readFloat(boolean isArray, T[] strs, int off, int len,
			Consumer<Float> setter) {
		float[] floatVals = null;
		if(isArray) {
			floatVals = new float[len];
		}

		for(int i = off, size = off + len; i < size; i++) {
			float floatVal = 0;
			try {
				floatVal = Float.parseFloat(strs[i].toString());
			} catch(Exception e) {
				unknownInputArg(ParameterType.FLOAT, strs[i], e);
			}
			if(!isArray) {
				try {
					setter.accept(floatVal);
				} catch (Exception e) {
					setterException(setter, floatVal, e);
				}
			}
			else {
				floatVals[i - off] = floatVal;
			}
		}
		if(isArray) {
			((Consumer<float[]>)(Consumer)setter).accept(floatVals);
		}
	}


	private static final <T extends CharSequence> void readInteger(boolean isArray, T[] strs, int off, int len,
			Consumer<Integer> setter) {
		int[] intVals = null;
		if(isArray) {
			intVals = new int[len];
		}

		for(int i = off, size = off + len; i < size; i++) {
			int intVal = 0;
			try {
				intVal = Integer.parseInt(strs[i].toString());
			} catch(Exception e) {
				unknownInputArg(ParameterType.INTEGER, strs[i], e);
			}
			if(!isArray) {
				try {
					setter.accept(intVal);
				} catch (Exception e) {
					setterException(setter, intVal, e);
				}
			}
			else {
				intVals[i - off] = intVal;
			}
		}
		if(isArray) {
			((Consumer<int[]>)(Consumer)setter).accept(intVals);
		}
	}


	private static final <T extends CharSequence> void readPath(boolean isArray, T[] strs, int off, int len,
			Consumer<Path> setter) {
		Path[] pathVals = null;
		if(isArray) {
			pathVals = new Path[len];
		}

		for(int i = off, size = off + len; i < size; i++) {
			Path pathVal = null;
			try {
				pathVal = Paths.get(strs[i].toString());
			} catch(Exception e) {
				unknownInputArg(ParameterType.PATH, strs[i], e);
			}
			if(!isArray) {
				try {
					setter.accept(pathVal);
				} catch (Exception e) {
					setterException(setter, pathVal, e);
				}
			}
			else {
				pathVals[i - off] = pathVal;
			}
		}
		if(isArray) {
			((Consumer<Path[]>)(Consumer)setter).accept(pathVals);
		}
	}


	private static final <T extends CharSequence> void readText(boolean isArray, T[] strs, int off, int len,
			Consumer<String> setter) {
		String[] textVals = null;
		if(isArray) {
			textVals = new String[len];
		}

		for(int i = off, size = off + len; i < size; i++) {
			String textVal = null;
			try {
				textVal = strs[i].toString();
			} catch(Exception e) {
				unknownInputArg(ParameterType.TEXT, strs[i], e);
			}
			if(!isArray) {
				try {
					setter.accept(textVal);
				} catch (Exception e) {
					setterException(setter, textVal, e);
				}
			}
			else {
				textVals[i - off] = textVal;
			}
		}
		if(isArray) {
			((Consumer<String[]>)(Consumer)setter).accept(textVals);
		}
	}

}
