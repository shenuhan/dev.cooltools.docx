package dev.cooltools.docx.core.evaluation;

import java.util.List;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;

public class DelegatingEvaluationContext implements EvaluationContext {
	private final EvaluationContext evaluationContext;
	private EvaluationContext temporaryEvaluationContext;

	public DelegatingEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}

	public void setTemporaryEvaluationContext(EvaluationContext evaluationContext) {
		this.temporaryEvaluationContext = evaluationContext;
	}

	public EvaluationContext getEvaluationContext() {
		return temporaryEvaluationContext == null ? evaluationContext : temporaryEvaluationContext;
	}

	public void restoreEvaluationContext() {
		this.temporaryEvaluationContext = null;
	}

	@Override
	public TypedValue getRootObject() {
		return new TypedValue(new RootObjectDelegator(() -> this.getEvaluationContext().getRootObject()));
	}

	@Override
	public List<PropertyAccessor> getPropertyAccessors() {
		return getEvaluationContext().getPropertyAccessors();
	}

	@Override
	public List<ConstructorResolver> getConstructorResolvers() {
		return getEvaluationContext().getConstructorResolvers();
	}

	@Override
	public List<MethodResolver> getMethodResolvers() {
		return getEvaluationContext().getMethodResolvers();
	}

	@Override
	public BeanResolver getBeanResolver() {
		return getEvaluationContext().getBeanResolver();
	}

	@Override
	public TypeLocator getTypeLocator() {
		return getEvaluationContext().getTypeLocator();
	}

	@Override
	public TypeConverter getTypeConverter() {
		return getEvaluationContext().getTypeConverter();
	}

	@Override
	public TypeComparator getTypeComparator() {
		return getEvaluationContext().getTypeComparator();
	}

	@Override
	public OperatorOverloader getOperatorOverloader() {
		return getEvaluationContext().getOperatorOverloader();
	}

	@Override
	public void setVariable(String name, Object value) {
		getEvaluationContext().setVariable(name, value);
	}

	@Override
	public Object lookupVariable(String name) {
		return getEvaluationContext().lookupVariable(name);
	}
}
