package programParameter;

import java.util.Map;
import java.util.function.Consumer;

/** Information about a parameter object.
 * A parameter contains the data type and parsing logic
 * for a {@link ParameterType}.  As well as a {@link #getSetter() setter} that
 * is called when a value is successfully parsed.
 * @author TeamworkGuy2
 * @since 2014-11-18
 * @param <T> the parameter's name type
 * @param <E> the data type of the parameter
 */
public interface ParameterMetaData<T, E> extends ParameterName<T>, ParameterAliases<T>, ParameterHelp {

	/**
	 * @return the data type of this parameter
	 */
	public ParameterType getParameterType();


	/**
	 * @return true if the parameter data type is an array, false if not
	 */
	public boolean isParameterArrayType();


	/**
	 * @return a mapping of the recognized enum names and values of this parameter.
	 * This method only returns a map if if this parameter meta data has a
	 * {@link ParameterType} of {@link ParameterType#ENUM ENUM}, else null is returned.
	 * @see #getParameterType()
	 */
	public Map<String, E> getEnumMap();


	public boolean isRequired();


	/**
	 * @return the setter function that sets this parameter's value
	 */
	public Consumer<E> getSetter();


	/** Check if the input name matches this parameter's name or one of its aliases
	 * @param inputName the input name to check
	 * @return true if {@code inputName} matches this parameter's name or aliases
	 */
	public default boolean isParameterName(T inputName) {
		return getPrimaryName().equals(inputName) || getAliases().contains(inputName);
	}


	/** Parse the specified inputs and call this parameter's setter with each of
	 * the parsed values
	 * @param inputs the array of inputs to parse
	 */
	public void parse(T[] inputs);


	/** Parse the specified inputs and call this parameter's setter with each of
	 * the parsed values
	 * @param inputs the array of inputs to parse
	 * @param off the offset into {@code inputs} at which to start parsing
	 * @param len the number of elements to parse from {@code inputs}
	 */
	public void parse(T[] inputs, int off, int len);

}
