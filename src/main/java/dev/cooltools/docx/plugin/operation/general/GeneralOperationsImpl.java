package dev.cooltools.docx.plugin.operation.general;

import org.apache.commons.lang3.StringUtils;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.error.ErrorType;

public class GeneralOperationsImpl implements GeneralOperations {
	private final Context context;

	public GeneralOperationsImpl(Context fullContext) {
		this.context = fullContext;
	}

	@Override
	public boolean set(String name, Object value) {
		if (StringUtils.isBlank(name)) {
			this.context.getProblemReporter().reportError(ErrorType.VariableNameNotEmpty);
		} else if (this.context.getExpressionEvaluator().existsVariable(name)) {
			this.context.getProblemReporter().reportError(ErrorType.ExistingVariable, name);
		} else {
			this.context.getExpressionEvaluator().addVariable(name, value);
		}
		return true;
	}
}
