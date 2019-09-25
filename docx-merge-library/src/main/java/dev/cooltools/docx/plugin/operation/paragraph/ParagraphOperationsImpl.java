package dev.cooltools.docx.plugin.operation.paragraph;

import java.util.Collection;
import java.util.List;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.LoopManager;
import dev.cooltools.docx.plugin.LoopManager.LoopType;

public class ParagraphOperationsImpl implements ParagraphOperations {
	private final ParagraphManager paragraphManager;
	
	@InjectPluginBean
	private LoopManager loopManager;

	public ParagraphOperationsImpl(Context context) {
		this.paragraphManager = new ParagraphManager(context);
	}

	@Override
	public boolean show(boolean show) {
		if (!show) {
			paragraphManager.hideParagraph(true);
		}
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
		if (objects == null || objects.isEmpty()) {
			return hide();
		}
		loopManager.loop(List.copyOf(objects), variableName, LoopType.Paragraph);

		return true;
	}
}
