package dev.cooltools.docx.core.evaluation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SafeEvaluationContext extends StandardEvaluationContext {
	public SafeEvaluationContext(Map<String, Object> operations, Map<String, Object> variables) {
		super(operations);
		this.setVariables(variables);
		this.addPropertyAccessor(new JsonNodePropertyAccessor());
		this.addPropertyAccessor(new MapAccessor());
		this.addPropertyAccessor(new RootObjectDelegatorAccessor());
	}

	@Override
	public List<ConstructorResolver> getConstructorResolvers() {
		return Collections.emptyList();
	}

	@Override
	public TypeLocator getTypeLocator() {
		return null;
	}

	@Override
	public BeanResolver getBeanResolver() {
		return null;
	}
}
