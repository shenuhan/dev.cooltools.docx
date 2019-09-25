package dev.cooltools.docx.plugin.operation;

import org.apache.commons.lang3.StringUtils;

import dev.cooltools.docx.core.Context;

public class StringUtilsOperationPlugin implements OperationPlugin<StringUtils> {
	private static final StringUtils utils = new StringUtils();
	
	@Override
	public StringUtils bean(Context context) {
		return utils;
	}

	@Override
	public String[] prefixes() {
		return new String[] {"su", "stringutils"};
	}
}
