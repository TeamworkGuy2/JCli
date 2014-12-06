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


	public ParameterBuilder<C, T> setSetter(Consumer<T> setter);


	public ParameterMetaData<C, T> build();


	public static <C extends CharSequence> ParameterBuilderImpl<C, Boolean> newFlag(Class<Boolean> classType) {
		return new ParameterBuilderImpl<C, Boolean>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Float> newFloat(Class<Float> classType) {
		return new ParameterBuilderImpl<C, Float>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Integer> newInteger(Class<Integer> classType) {
		return new ParameterBuilderImpl<C, Integer>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Path> newPath(Class<Path> classType) {
		return new ParameterBuilderImpl<C, Path>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, String> newText(Class<String> classType) {
		return new ParameterBuilderImpl<C, String>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Boolean> newFlagArray(Class<Boolean[]> classType) {
		return new ParameterBuilderImpl<C, Boolean>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Float[]> newFloatArray(Class<Float[]> classType) {
		return new ParameterBuilderImpl<C, Float[]>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Integer[]> newIntegerArray(Class<Integer[]> classType) {
		return new ParameterBuilderImpl<C, Integer[]>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, Path[]> newPathArray(Class<Path[]> classType) {
		return new ParameterBuilderImpl<C, Path[]>(classType, false);
	}


	public static <C extends CharSequence> ParameterBuilderImpl<C, String[]> newTextArray(Class<String[]> classType) {
		return new ParameterBuilderImpl<C, String[]>(classType, false);
	}


	public static <C extends CharSequence, E extends Enum<E>> ParameterBuilderImpl<C, E> newEnum(
			Class<E> enumClass) {
		return ParameterBuilderImpl.<C, E>newEnumParameterBuilder(enumClass);
	}


	public static <C extends CharSequence, E extends Enum<E>> ParameterBuilderImpl<C, E> newEnumArray(
			Class<E[]> enumArrayClass) {
		return ParameterBuilderImpl.<C, E>newEnumArrayParameterBuilder(enumArrayClass);
	}


	public static <C extends CharSequence, E> ParameterBuilderImpl<C, E> newEnumMap(
			Map<String, E> enumsMap, Class<E> classType) {
		return ParameterBuilderImpl.<C, E>newEnumMapParameterBuilder(enumsMap, classType);
	}


	public static <C extends CharSequence, E> ParameterBuilderImpl<C, E> newEnumMapArray(
			Map<String, E> enumsMap, Class<E[]> classType) {
		return ParameterBuilderImpl.<C, E>newEnumArrayMapParameterBuilder(enumsMap, classType);
	}

}
