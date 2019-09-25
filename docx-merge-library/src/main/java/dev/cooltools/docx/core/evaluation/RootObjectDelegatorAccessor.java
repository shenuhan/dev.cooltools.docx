package dev.cooltools.docx.core.evaluation;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

public class RootObjectDelegatorAccessor implements PropertyAccessor {
	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof RootObjectDelegator) {
			TypedValue rootObject = ((RootObjectDelegator) target).getRootObjectProvider().get();
			for (var accessor : context.getPropertyAccessors()) {
				if (accessor.canRead(context, rootObject.getValue(), name)) {
					return accessor.read(context, rootObject.getValue(), name);
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] { RootObjectDelegator.class };
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target instanceof RootObjectDelegator;
	}

}
