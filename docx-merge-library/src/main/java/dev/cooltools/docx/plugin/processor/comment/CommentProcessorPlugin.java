package dev.cooltools.docx.plugin.processor.comment;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.processor.ProcessorPlugin;

public class CommentProcessorPlugin implements ProcessorPlugin<CommentProcessor> {
	@Override
	public CommentProcessor bean(Context context) {
		return new CommentProcessor(context);
	}
}
