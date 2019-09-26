package dev.cooltools.docx.core.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

public class NopeEvaluationContext implements EvaluationContext {
	static public class NopeObject {
	}
	static NopeObject nope = new NopeObject();
	static TypedValue nopeValue = new TypedValue(nope);

	final private Object root;
	final private List<MethodResolver> methodResolvers = new ArrayList<>();
	final private List<PropertyAccessor> propertyAccessors = new ArrayList<>();

	public NopeEvaluationContext(Map<String, Object> operations) {
		this.root = operations;

		var reflectiveMethod = new ReflectiveMethodResolver();
		methodResolvers.add(
			(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes) -> {
				try {
					var result = reflectiveMethod.resolve(context, targetObject, name, argumentTypes);
					if (result != null) {
						return result;
					}
				} catch (Exception e) {
					// We dont thropw exceptions, we tolerate every call
				}
				return new MethodExecutor() {
					@Override
					public TypedValue execute(EvaluationContext context, Object target, Object... arguments) throws AccessException {
						return new TypedValue(new NopeObject());
					}
				};
			}
		);

		var reflectiveAccessor = new ReflectivePropertyAccessor();
		propertyAccessors.add(new JsonNodePropertyAccessor());
		propertyAccessors.add(new RootObjectDelegatorAccessor());
		propertyAccessors.add(new MapAccessor() {
			@Override
			public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
				var read = super.read(context, target, name);
				if (read == null || read.getValue() == null) {
					return new TypedValue(new NopeObject());
				}
				return read;
			}
		});
		propertyAccessors.add(new PropertyAccessor() {
			@Override
			public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
			}

			@Override
			public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
				return nopeValue;
			}

			@Override
			public Class<?>[] getSpecificTargetClasses() {
				return new Class<?>[] { Object.class };
			}

			@Override
			public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
				return true;
			}

			@Override
			public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
				return !reflectiveAccessor.canRead(context, target, name);
			}
		});
		propertyAccessors.add(reflectiveAccessor);
	}

	@Override
	public TypedValue getRootObject() {
		return new TypedValue(root);
	}

	@Override
	public List<ConstructorResolver> getConstructorResolvers() {
		throw new IllegalAccessError("You cannot call constructor in the document");
	}

	@Override
	public List<MethodResolver> getMethodResolvers() {
		return methodResolvers;
	}

	@Override
	public List<PropertyAccessor> getPropertyAccessors() {
		return propertyAccessors;
	}

	@Override
	public TypeLocator getTypeLocator() {
		throw new IllegalAccessError("You cannot find type in the document");
	}

	@Override
	public TypeConverter getTypeConverter() {
		return new TypeConverter() {
			@Override
			public Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
				if (Collection.class.isAssignableFrom(targetType.getObjectType()) && value instanceof NopeObject) {
					return List.of(nope);
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
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
				return true;
			}
		};
	}

	@Override
	public TypeComparator getTypeComparator() {
		return new TypeComparator() {
			@Override
			public int compare(Object firstObject, Object secondObject) throws EvaluationException {
				return -1;
			}

			@Override
			public boolean canCompare(Object firstObject, Object secondObject) {
				return true;
			}
		};
	}

	@Override
	public OperatorOverloader getOperatorOverloader() {
		throw new IllegalAccessError("You cannot call overload");
	}

	@Override
	public BeanResolver getBeanResolver() {
		throw new IllegalAccessError("You cannot call bean resolver in the document");
	}

	@Override
	public Object lookupVariable(String name) {
		return nope;
	}

	@Override
	public void setVariable(String name, Object value) {
	}
}
