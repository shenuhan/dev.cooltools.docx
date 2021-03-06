package dev.cooltools.docx.plugin;

import dev.cooltools.docx.core.Context;

public interface Plugin<BeanType> {
	/**
	 * This return the initialized bean corresponding to this document
	 * The bean will be released at the end of the processing
	 * @param context the context of the processing
	 * @return an instance generated by the plugin
	 */
	BeanType bean(Context context);
}
