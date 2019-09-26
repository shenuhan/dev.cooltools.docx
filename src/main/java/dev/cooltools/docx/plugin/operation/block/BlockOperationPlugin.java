package dev.cooltools.docx.plugin.operation.block;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.operation.OperationPlugin;

public class BlockOperationPlugin implements OperationPlugin<BlockOperations> {
	@Override
	public BlockOperations bean(Context context) {
		return new BlockOperationsImpl();
	}

	@Override
	public String[] prefixes() {
		return new String[] {"b", "block"};
	}
}
