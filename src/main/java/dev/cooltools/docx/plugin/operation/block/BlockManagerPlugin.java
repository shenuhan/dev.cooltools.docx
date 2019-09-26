package dev.cooltools.docx.plugin.operation.block;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.Plugin;

public class BlockManagerPlugin implements Plugin<BlockManager> {
	@Override
	public BlockManager bean(Context context) {
		return new BlockManager(context);
	}
}
