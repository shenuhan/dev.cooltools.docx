package dev.cooltools.docx.plugin.operation.paragraph;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.operation.OperationPlugin;

public class ParagraphOperationPlugin implements OperationPlugin<ParagraphOperations> {
	@Override
	public ParagraphOperations bean(Context context) {
		return new ParagraphOperationsImpl(context);
	}

	@Override
	public String[] prefixes() {
		return new String[] {"p", "paragraph"};
	}
}
