package dev.cooltools.docx.core.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;

import dev.cooltools.docx.core.evaluation.NopeEvaluationContext.NopeObject;
import dev.cooltools.docx.service.Property;
import dev.cooltools.docx.service.Property.VariableType;

public class DefaultTypeConverter implements TypeConverter {
	@Override
	public Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (value instanceof Property) {
			Property val = (Property) value;
			if (Collection.class.isAssignableFrom(targetType.getObjectType())) {
				val.setType(VariableType.List);
				return Arrays.asList(val);
			} else if (Number.class.isAssignableFrom(targetType.getObjectType())) {
				val.setType(VariableType.Number);
			} else if (Temporal.class.isAssignableFrom(targetType.getObjectType())) {
				val.setType(VariableType.Date);
			} else if (CharSequence.class.isAssignableFrom(targetType.getObjectType())) {
				val.setType(VariableType.String);
			} else if (Boolean.class.isAssignableFrom(targetType.getObjectType())) {
				val.setType(VariableType.Boolean);
			}
		} else if (value instanceof NopeObject) {
			NopeObject val = (NopeObject) value;
			if (Collection.class.isAssignableFrom(targetType.getObjectType())) {
				return Arrays.asList(val);
			}
		}
		if (targetType.getObjectType().isAssignableFrom(value.getClass())) {
			return value;
		}
		return getDefault(targetType.getObjectType());
	}

	private Object getDefault(Class<?> objectType) {
		if (objectType == Boolean.class || objectType == boolean.class) {
			return true;
		} else if (objectType == Integer.class || objectType == int.class) {
			return 0;
		} else if (objectType == Long.class || objectType == long.class) {
			return 0l;
		} else if (objectType == Float.class || objectType == float.class) {
			return 0f;
		} else if (objectType == Double.class || objectType == double.class) {
			return 0d;
		}
		try {
			return objectType.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	@Override
	public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return true;
	}
}