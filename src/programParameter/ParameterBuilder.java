package programParameter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Interface for {@link ParameterMetaData} builders
 * 
 * @author TeamworkGuy2
 * @since 2014-11-19
 * 
 * @param <C> the type of the name of the parameter being built, normally {@link String}
 * @param <T> the data type of the parameter, such as {@link Integer} or {@link String}
 */
public interface ParameterBuilder<C extends CharSequence, T> {

	public ParameterType getParameterType();


	public ParameterBuilder<C, T> setParameterType(ParameterType paramType);


	public boolean getIsArrayType();


	public ParameterBuilder<C, T> setIsArrayType(boolean isArrayType);


	/** Get the enum map associated with this parameter if it is an {@link ParameterType#ENUM}
	 * @return a map of the enum names and constants that are valid for this
	 * parameter type, or null if the {@link #getParameterType()} is not {@link ParameterType#ENUM}
	 */
	public <E extends Enum<E>> Map<String, E> getEnumNameMap();


	public C getPrimaryName();


	public List<C> getAliases();


	/**
	 * @param primaryName the primary name of this parameter, this value should
	 * not be repeated in the list of {@code aliases}
	 * @param aliases a list of aliases that are recognized as valid substitutes for the name of this parameter
	 * @return this ParameterBuilder
	 */
	public ParameterBuilder<C, T> setNameAndAliases(C primaryName, 	@SuppressWarnings("unchecked") C... aliases);


	public String getHelpMessage();


	public ParameterBuilder<C, T> setHelpMessage(CharSequence helpMessage);


	public String getRequestParameterMessage();


	public ParameterBuilder<C, T> setRequestParameterMessage(CharSequence requestParameterMessage);


	public boolean isRequired();


	public ParameterBuilder<C, T> setRequired(boolean required);


	public Consumer<T> getSetter();


	/** A function to call with the results of successfully parsing parameters.
	 * If this parameter type is an array, the function is expected to accept an array
	 * of the primitive type of this parameter.  For example, if the parameter type is
	 * an array of {@link ParameterType#FLAG}, the function should accept a {@code boolean[]}.
	 * If the parameter type is an array of {@link ParameterType#TEXT}, the function should
	 * accept a {@code String[]}
	 * @param setter
	 * @return
	 */
	public ParameterBuilder<C, T> setSetter(Consumer<T> setter);


	public ParameterMetaData<C, T> build();


	public static ParameterBuilderImpl<String, Boolean> newFlag() {
		return new ParameterBuilderImpl<>(ParameterType.FLAG, false);
	}


	public static ParameterBuilderImpl<String, Float> newFloat() {
		return new ParameterBuilderImpl<>(ParameterType.FLOAT, false);
	}


	public static ParameterBuilderImpl<String, Integer> newInteger() {
		return new ParameterBuilderImpl<>(ParameterType.INTEGER, false);
	}


	public static ParameterBuilderImpl<String, Path> newPath() {
		return new ParameterBuilderImpl<>(ParameterType.PATH, false);
	}


	public static ParameterBuilderImpl<String, String> newText() {
		return new ParameterBuilderImpl<>(ParameterType.TEXT, false);
	}


	public static ParameterBuilderImpl<String, Boolean> newFlagArray() {
		return new ParameterBuilderImpl<>(ParameterType.FLAG, true);
	}


	public static ParameterBuilderImpl<String, Float[]> newFloatArray() {
		return new ParameterBuilderImpl<>(ParameterType.FLOAT, true);
	}


	public static ParameterBuilderImpl<String, Integer[]> newIntegerArray() {
		return new ParameterBuilderImpl<>(ParameterType.INTEGER, true);
	}


	public static ParameterBuilderImpl<String, Path[]> newPathArray() {
		return new ParameterBuilderImpl<>(ParameterType.PATH, true);
	}


	public static ParameterBuilderImpl<String, String[]> newTextArray() {
		return new ParameterBuilderImpl<>(ParameterType.TEXT, true);
	}


	public static <E extends Enum<E>> ParameterBuilderImpl<String, E> newEnum(
			Class<E> enumClass) {
		return ParameterBuilderImpl.<String, E>newEnumParameterBuilder(enumClass);
	}


	public static <E extends Enum<E>> ParameterBuilderImpl<String, E> newEnumArray(
			Class<E[]> enumArrayClass) {
		return ParameterBuilderImpl.<String, E>newEnumArrayParameterBuilder(enumArrayClass);
	}


	public static <E> ParameterBuilderImpl<String, E> newEnumMap(
			Map<String, E> enumsMap, Class<E> classType) {
		return ParameterBuilderImpl.<String, E>newEnumMapParameterBuilder(enumsMap, classType);
	}


	public static <E> ParameterBuilderImpl<String, E> newEnumMapArray(
			Map<String, E> enumsMap, Class<E[]> classType) {
		return ParameterBuilderImpl.<String, E>newEnumArrayMapParameterBuilder(enumsMap, classType);
	}

}
