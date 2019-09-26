package dev.cooltools.docx.plugin.operation.text;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.operation.OperationPlugin;

public class TextOperationPlugin implements OperationPlugin<TextOperations> {
	@Override
	public TextOperations bean(Context context) {
		return new TextOperationsImpl(context);
	}

	@Override
	public String[] prefixes() {
		return new String[] {"t", "text"};
	}
}
