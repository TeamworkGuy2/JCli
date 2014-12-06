package programParameter;

import java.util.List;

/** An interface representing an object that has aliases
 * @param <T> the type of aliases
 * @author TeamworkGuy2
 * @since 2014-5-21
 */
@FunctionalInterface
public interface ParameterAliases<T> {

	public List<T> getAliases();

}
