package twg2.cli;

/**
 * @author TeamworkGuy2
 * @since 2014-11-16
 * @param <T> the type of name this parameter has, this is normally a {@link String} or
 * {@link CharSequence} of some type
 */
public interface ParameterName<T> {

	public T getPrimaryName();

}
