package dev.cooltools.docx.plugin;

import dev.cooltools.docx.core.Context;

public class LoopManagerPlugin implements Plugin<LoopManager> {
	@Override
	public LoopManager bean(Context context) {
		return new LoopManager(context);
	}

}
