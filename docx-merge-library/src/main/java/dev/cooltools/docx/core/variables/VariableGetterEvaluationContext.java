package dev.cooltools.docx.core.variables;

import java.lang.reflect.InvocationTargetException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import dev.cooltools.docx.core.evaluation.MapAccessor;
import dev.cooltools.docx.core.evaluation.RootObjectDelegatorAccessor;
import dev.cooltools.docx.error.ProblemReporter;
import dev.cooltools.docx.service.Property;
import dev.cooltools.docx.service.PropertyImpl;
import dev.cooltools.docx.service.Property.VariableType;

public class VariableGetterEvaluationContext implements EvaluationContext {
	final private Object root;
	final private Map<String, Object> settedVariables = new HashMap<>();
	final private List<Property> variables;
	final private List<MethodResolver> methodResolvers = new ArrayList<>();
	final private List<PropertyAccessor> propertyAccessors = new ArrayList<>();
	final private TypeConverter typeConverter;
	final private ProblemReporter reporter;

	public VariableGetterEvaluationContext(Map<String, Object> operations, List<Property> variableTree, ProblemReporter reporter) {
		this.root = operations;
		this.variables = variableTree;
		this.reporter = reporter;

		methodResolvers.add(new MethodResolver() {
			@Override
			public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes) throws AccessException {
				if (!(targetObject instanceof Property))
					return null;
				return new MethodExecutor() {
					@Override
					public TypedValue execute(EvaluationContext context, Object target, Object... arguments) throws AccessException {
						if (target instanceof Property) {
							Property object = (Property) target;
							return new TypedValue(object.addProperty(name, VariableType.Method));
						}
						return null;
					}
				};
			}
		});
		methodResolvers.add(new ReflectiveMethodResolver());

		propertyAccessors.add(new PropertyAccessor() {
			@Override
			public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
			}

			@Override
			public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
				Property object = (Property) target;
				Property p = object.getProperties().stream().filter(pr -> pr.getName().equals(name)).findAny().orElse(null);
				return new TypedValue(p != null ? p : object.addProperty(name, VariableType.Unknown));
			}

			@Override
			public Class<?>[] getSpecificTargetClasses() {
				return new Class<?>[] { Property.class };
			}

			@Override
			public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
				return true;
			}

			@Override
			public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
				return (target instanceof Property);
			}
		});
		propertyAccessors.add(new MapAccessor());
		propertyAccessors.add(new RootObjectDelegatorAccessor());
		var reflectiveAccessor = new ReflectivePropertyAccessor();
		propertyAccessors.add(reflectiveAccessor);

		this.typeConverter = new TypeConverter() {
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
		throw new IllegalAccessError("You cannot call constructor in the document");
	}

	@Override
	public TypeConverter getTypeConverter() {
		return this.typeConverter;
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
	public void setVariable(String name, Object value) {
		if (value == null) {
			settedVariables.remove(name);
		} else {
			settedVariables.put(name, value);
		}
	}

	@Override
	public Object lookupVariable(String name) {
		if (settedVariables.containsKey(name)) {
			return settedVariables.get(name);
		}

		Property property = variables.stream().filter(p -> p.getName().equals(name)).findAny().orElse(null);

		if (property == null) {
			property = new PropertyImpl(name, VariableType.Unknown, reporter);
			variables.add(property);
		}
		return property;
	}

	public boolean existsVariable(String name) {
		return settedVariables.containsKey(name);
	}
}
