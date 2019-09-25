package dev.cooltools.docx.plugin.processor;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.Plugin;

public class MultiRunManagerPlugin implements Plugin<MultiRunManager> {
	@Override
	public MultiRunManager bean(Context context) {
		return new MultiRunManager();
	}
}
