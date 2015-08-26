package programParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import twg2.collections.util.MapBuilder;

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
	private Predicate<T> validator;
	private Function<T, String> validatorMsgs;


	ParameterBuilderImpl(Map<String, ? extends T> enumsMap, Class<T> dataType, boolean isEnum, boolean isArray) {
		this(dataType, isEnum);
		@SuppressWarnings("unchecked")
		Map<String, T> enumMapE = (Map<String, T>) enumsMap;
		this.enumMap = enumMapE;
	}


	public ParameterBuilderImpl(ParameterType type, boolean isArray) {
		this(isArray ? type.getArrayDataType() : type.getDefaultDataType(), type == ParameterType.ENUM);
	}


	// package-private
	ParameterBuilderImpl(Class<?> classType, boolean isEnum) {
		this.isArray = classType.isArray();
		if(this.isArray) {
			classType = classType.getComponentType();
		}

		boolean foundType = false;
		for(ParameterType type : ParameterType.values()) {
			if(type.isDataTypeClass(classType)) {
				this.type = type;
				foundType = true;
				break;
			}
		}
		if(!foundType) {
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
	public Predicate<T> getValidator() {
		return validator;
	}


	@Override
	public ParameterBuilder<C, T> setValidator(Predicate<T> validator) {
		this.validator = validator;
		return this;
	}


	@Override
	public Function<T, String> getValidatorMessageGenerator() {
		return validatorMsgs;
	}


	@Override
	public ParameterBuilder<C, T> setValidatorMessageGenerator(Function<T, String> validatorMessageGenerator) {
		this.validatorMsgs = validatorMessageGenerator;
		return this;
	}


	@Override
	public ParameterData<C, T> build() {
		ParameterData<C, T> param = null;
		if(enumMap == null) {
			param = new ParameterDataImpl<>(type, isArray, primaryName, aliases,
					setter, validator, validatorMsgs, helpMsg, requestParamMsg, required);
		}
		else {
			param = new ParameterDataImpl<>(type, isArray, enumMap, primaryName, aliases,
					setter, validator, validatorMsgs, helpMsg, requestParamMsg, required);
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
		@SuppressWarnings("unchecked")
		Class<E> enumType = (Class<E>)enumArrayClass.getComponentType();
		Map<String, E> enumMap = MapBuilder.newMutableEnumNames(enumType);
		return new ParameterBuilderImpl<C, E>(enumMap, enumType, true, false);
	}


	public static final <C extends CharSequence, T> ParameterBuilderImpl<C, T> newEnumMapParameterBuilder(
			Map<String, ? extends T> enumsMap, Class<T> dataType) {
		@SuppressWarnings("unchecked")
		Map<String, T> enumMap = (Map<String, T>)enumsMap;
		return new ParameterBuilderImpl<C, T>(enumMap, dataType, true, false);
	}


	public static final <C extends CharSequence, T> ParameterBuilderImpl<C, T> newEnumArrayMapParameterBuilder(
			Map<String, ? extends T> enumsMap, Class<T[]> dataType) {
		@SuppressWarnings("unchecked")
		Class<T> enumType = (Class<T>)dataType.getComponentType();
		@SuppressWarnings("unchecked")
		Map<String, T> enumMap = (Map<String, T>)enumsMap;
		return new ParameterBuilderImpl<C, T>(enumMap, enumType, true, true);
	}

}
