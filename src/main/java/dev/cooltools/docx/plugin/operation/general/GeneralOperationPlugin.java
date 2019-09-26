package dev.cooltools.docx.plugin.operation.general;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.operation.OperationPlugin;

public class GeneralOperationPlugin implements OperationPlugin<GeneralOperations> {
	@Override
	public GeneralOperations bean(Context context) {
		return new GeneralOperationsImpl(context);
	}

	@Override
	public String[] prefixes() {
		return new String[] {"op"};
	}
}
