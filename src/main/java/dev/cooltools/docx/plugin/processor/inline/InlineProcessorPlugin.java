package dev.cooltools.docx.plugin.processor.inline;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.processor.ProcessorPlugin;

public class InlineProcessorPlugin implements ProcessorPlugin<InlineProcessor> {
	@Override
	public InlineProcessor bean(Context context) {
		return new InlineProcessor(context);
	}
}
