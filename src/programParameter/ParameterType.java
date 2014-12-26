package programParameter;

import java.nio.file.Path;

/** A set of parsable parameter data types
 * @author TeamworkGuy2
 * @since 2014-11-16
 */
public enum ParameterType {
	ENUM(Enum.class, null, Enum[].class, null) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return Enum.class.isAssignableFrom(type);
		}
	},
	FLAG(Boolean.class, Boolean.TYPE, Boolean[].class, boolean[].class) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return type == Boolean.TYPE || type == Boolean.class;
		}
	},
	FLOAT(Float.class, Float.TYPE, Float[].class, float[].class) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return type == Float.TYPE || type == Float.class;
		}
	},
	INTEGER(Integer.class, Integer.TYPE, Integer[].class, int[].class) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return type == Integer.TYPE || type == Integer.class;
		}
	},
	PATH(Path.class, null, Path[].class, null) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return Path.class.isAssignableFrom(type);
		}
	},
	TEXT(String.class, null, String.class, null) {
		@Override public boolean isDataTypeClass(Class<?> type) {
			return String.class.isAssignableFrom(type);
		}
	};


	private final Class<?> defaultDataType;
	private final Class<?> primitiveDataType;
	private final Class<?> defaultArrayType;
	private final Class<?> primitiveArrayType;


	ParameterType(Class<?> defaultDataType, Class<?> primitiveDataType, Class<?> defaultArrayType, Class<?> primitiveArrayType) {
		this.defaultDataType = defaultDataType;
		this.primitiveDataType = primitiveDataType;
		this.defaultArrayType = defaultArrayType;
		this.primitiveArrayType = primitiveArrayType;
	}


	public abstract boolean isDataTypeClass(Class<?> type);


	public Class<?> getDefaultDataType() {
		return primitiveDataType != null ? primitiveDataType : defaultDataType;
	}


	public Class<?> getArrayDataType() {
		return primitiveArrayType != null ? primitiveArrayType : defaultArrayType;
	}


	/**
	 * @return the object data type of this parameter type, for example {@link Integer}
	 */
	public Class<?> getObjectDataType() {
		return this.defaultDataType;
	}


	/**
	 * @return the primitive data type of this parameter type, for example {@link Integer#TYPE}
	 */
	public Class<?> getPrimitiveDataType() {
		return this.primitiveDataType;
	}

}
