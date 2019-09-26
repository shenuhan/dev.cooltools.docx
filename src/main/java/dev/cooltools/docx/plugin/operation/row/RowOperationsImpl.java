package dev.cooltools.docx.plugin.operation.row;

import java.util.Collection;

import dev.cooltools.docx.core.Context;

public class RowOperationsImpl implements RowOperations {
	private final RowManager rowManager;

	public RowOperationsImpl(Context context) {
		this.rowManager = new RowManager(context);
	}

	@Override
	public boolean show(boolean show) {
		if (!show)
			rowManager.hide();
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
		return rowManager.loop(objects, variableName);
	}
}
