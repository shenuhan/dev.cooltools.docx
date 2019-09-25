package dev.cooltools.docx.plugin.processor.property;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.processor.ProcessorPlugin;

public class PropertyProcessorPlugin implements ProcessorPlugin<PropertyProcessor> {
	@Override
	public PropertyProcessor bean(Context context) {
		return new PropertyProcessor(context);
	}

}
