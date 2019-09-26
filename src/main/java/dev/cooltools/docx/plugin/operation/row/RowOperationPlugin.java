package dev.cooltools.docx.plugin.operation.row;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.operation.OperationPlugin;

public class RowOperationPlugin implements OperationPlugin<RowOperations> {
	@Override
	public RowOperations bean(Context context) {
		return new RowOperationsImpl(context);
	}

	@Override
	public String[] prefixes() {
		return new String[] {"r", "row"};
	}
}
