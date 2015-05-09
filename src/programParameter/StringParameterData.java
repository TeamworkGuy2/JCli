package programParameter;

/** Information about a string parameter object.
 * A parameter contains the data type and parsing logic
 * for a {@link ParameterType}.  As well as a {@link #getSetter() setter} that
 * is called when a value is successfully parsed.
 * @author TeamworkGuy2
 * @since 2015-4-24
 * @param <E> the data type of the parameter
 */
public interface StringParameterData<E> extends ParameterData<String, E> {

}
