package dev.cooltools.docx.plugin.operation.block;

import java.util.Collection;

import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.LoopManager;
import dev.cooltools.docx.plugin.operation.SimpleOperations;

public class BlockOperationsImpl extends SimpleOperations implements BlockOperations {
	@InjectPluginBean
	private LoopManager loopManager;
	
	@InjectPluginBean
	private BlockManager blockManager;

	@Override
	public boolean show(boolean show) {
		blockManager.startBlock(show);
		return true;
	}

	@Override
	public boolean hide(boolean hide) {
		return show(!hide);
	}

	@Override
	public boolean hide() {
		return hide(true);
	}

	@Override
	public boolean repeat(Collection<Object> objects, String variableName) {
		blockManager.startLoop(objects, variableName);
		return true;
	}

	@Override
	public boolean end() {
		blockManager.endBlock();
		return true;
	}
}
