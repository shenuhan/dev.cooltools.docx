package dev.cooltools.docx.core.evaluation;

import java.util.Map;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

public class MapAccessor implements PropertyAccessor {
	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof Map) {
			return new TypedValue(((Map<?, ?>) target).get(name));
		}
		return null;
	}

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] { Map.class };
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target instanceof Map;
	}

}
