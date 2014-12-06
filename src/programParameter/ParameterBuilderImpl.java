package programParameter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dataUtility.MapBuilder;

public class ParameterBuilderImpl<C extends CharSequence, T> implements ParameterBuilder<C, T> {
	private ParameterType type;
	private Map<String, T> enumMap;
	private boolean isArray;
	private C primaryName;
	private List<C> aliases;
	private String helpMsg;
	private String requestParamMsg;
	private boolean required;
	private Consumer<T> setter;


	ParameterBuilderImpl(Map<String, ? extends T> enumsMap, Class<T> dataType, boolean isEnum, boolean isArray) {
		this(dataType, isEnum);
		@SuppressWarnings("unchecked")
		Map<String, T> enumMapE = (Map<String, T>) enumsMap;
		this.enumMap = enumMapE;
	}


	// package-private
	ParameterBuilderImpl(Class<?> classType, boolean isEnum) {
		this.isArray = classType.isArray();
		if(this.isArray) {
			classType = classType.getComponentType();
		}

		if(classType == Boolean.TYPE || classType == Boolean.class) {
			this.type = ParameterType.FLAG;
		}
		else if(classType == Float.TYPE || classType == Float.class) {
			this.type = ParameterType.FLOAT;
		}
		else if(classType == Integer.TYPE || classType == Integer.class) {
			this.type = ParameterType.INTEGER;
		}
		else if(Path.class.isAssignableFrom(classType)) {
			this.type = ParameterType.PATH;
		}
		else if(String.class.isAssignableFrom(classType)) {
			this.type = ParameterType.TEXT;
		}
		else if(isEnum && Enum.class.isAssignableFrom(classType)) {
			this.type = ParameterType.ENUM;
		}
		else {
			throw new IllegalArgumentException("the class '" + classType + "'" +
					" is not a recognized parameter type, a parameter type must be one of ParameterType's values");
		}
		this.aliases = new ArrayList<>();
	}


	@Override
	public ParameterType getParameterType() {
		return type;
	}


	@Override
	public ParameterBuilder<C, T> setParameterType(ParameterType paramType) {
		this.type = paramType;
		return this;
	}


	@Override
	public boolean getIsArrayType() {
		return isArray;
	}


	@Override
	public ParameterBuilder<C, T> setIsArrayType(boolean isArrayType) {
		this.isArray = isArrayType;
		return this;
	}


	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T> getEnumNameMap() {
		return enumMap;
	}


	@Override
	public C getPrimaryName() {
		return primaryName;
	}


	@Override
	public List<C> getAliases() {
		return aliases;
	}


	@Override
	@SafeVarargs
	public final ParameterBuilder<C, T> setNameAndAliases(C primaryName, C... aliases) {
		this.primaryName = primaryName;
		Collections.addAll(this.aliases, aliases);
		return this;
	}


	@Override
	public String getHelpMessage() {
		return helpMsg;
	}


	@Override
	public ParameterBuilder<C, T> setHelpMessage(CharSequence helpMessage) {
		this.helpMsg = helpMessage.toString();
		return this;
	}


	@Override
	public String getRequestParameterMessage() {
		return requestParamMsg;
	}


	@Override
	public ParameterBuilder<C, T> setRequestParameterMessage(CharSequence requestParameterMessage) {
		this.requestParamMsg = requestParameterMessage.toString();
		return this;
	}


	@Override
	public boolean isRequired() {
		return required;
	}


	@Override
	public ParameterBuilder<C, T> setRequired(boolean required) {
		this.required = required;
		return this;
	}


	@Override
	public Consumer<T> getSetter() {
		return setter;
	}


	@Override
	public ParameterBuilder<C, T> setSetter(Consumer<T> setter) {
		this.setter = setter;
		return this;
	}


	@Override
	public ParameterMetaData<C, T> build() {
		ParameterMetaData<C, T> param = null;
		if(enumMap == null) {
			param = new ParameterMetaDataImpl<>(type, isArray, primaryName, aliases,
					setter, helpMsg, requestParamMsg, required);
		}
		else {
			param = new ParameterMetaDataImpl<C, T>(type, isArray, enumMap, primaryName, aliases,
					setter, helpMsg, requestParamMsg, required);
		}
		return param;
	}


	/** Provides generic type safety for enum parameter builders
	 */
	public static final <C extends CharSequence, E extends Enum<E>> ParameterBuilderImpl<C, E> newEnumParameterBuilder(
				Class<E> enumClass) {
		if(enumClass.isArray()) {
			throw new IllegalArgumentException("this method creates a parameter parser for single enum values, " +
					"use newEnumArrayParameterBuilder() for parsing arrays of enum values");
		}
		Map<String, E> enumMap = MapBuilder.newMutableEnumNames(enumClass);
		return new ParameterBuilderImpl<C, E>(enumMap, enumClass, true, false);
	}


	/** Provides generic type safety for enum array parameter builders
	 */
	public static final <C extends CharSequence, E extends Enum<E>> ParameterBuilderImpl<C, E> newEnumArrayParameterBuilder(
			Class<E[]> enumArrayClass) {
		if(!enumArrayClass.isArray()) {
			throw new IllegalArgumentException("this method creates a parameter parser for arrays of enum values, " +
					"use newEnumParameterBuilder() for parsing single enum values");
		}
		Class<E> enumType = (Class<E>)enumArrayClass.getComponentType();
		Map<String, E> enumMap = MapBuilder.newMutableEnumNames(enumType);
		return new ParameterBuilderImpl<C, E>(enumMap, enumType, true, false);
	}


	public static final <C extends CharSequence, T> ParameterBuilderImpl<C, T> newEnumMapParameterBuilder(
			Map<String, ? extends T> enumsMap, Class<T> dataType) {
		return new ParameterBuilderImpl<C, T>((Map<String, T>)enumsMap, dataType, true, false);
	}


	public static final <C extends CharSequence, T> ParameterBuilderImpl<C, T> newEnumArrayMapParameterBuilder(
			Map<String, ? extends T> enumsMap, Class<T[]> dataType) {
		return new ParameterBuilderImpl<C, T>((Map<String, T>)enumsMap, (Class<T>)dataType.getComponentType(), true, true);
	}

}
