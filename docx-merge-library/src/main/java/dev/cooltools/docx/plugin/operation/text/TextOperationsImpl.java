package dev.cooltools.docx.plugin.operation.text;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.wml.R;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.processor.MultiRunManager;
import dev.cooltools.docx.util.RunUtil;

public class TextOperationsImpl implements TextOperations {
	private final Context context;
	private R currentRun;
	
	@InjectPluginBean
	private MultiRunManager multiRunManager;

	public TextOperationsImpl(Context context) {
		this.context = context;
		this.context.getDocumentCrawler().subscribe(DocxCrawler.ProcessRun, r -> currentRun = r);
	}

	@Override
	public boolean value(String value) {
		RunUtil.setText(currentRun, value);
		for (R r : multiRunManager.getRuns()) {
			if (r != currentRun) {
				context.getPostProcessor().registerRemoveElement(r);
			}
		}
		return true;
	}

	@Override
	public boolean show(boolean show) {
		if (!show) {
			for (R r : multiRunManager.getRuns()) {
				context.getPostProcessor().registerRemoveElement(r);
			}
		}
		return true;
	}

	@Override
	public boolean hide(boolean hide) {
		show(!hide);
		return true;
	}

	@Override
	public boolean hide() {
		hide(true);
		return true;
	}

	@Override
	public String join(Collection<String> values) {
		return StringUtils.join(values, "");
	}

	@Override
	public String join(Collection<String> values, String separator) {
		return StringUtils.join(values, separator);
	}

	@Override
	public String join(Collection<String> values, String separator, String lastSeparator) {
		if (values == null || values.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String value : values) {
			if (i == 0) {
				sb.append(value);
			} else if (i == values.size() - 1) {
				sb.append(lastSeparator).append(value == null ? "" : value);
			} else {
				sb.append(separator).append(value == null ? "" : value);
			}
			i++;
		}
		return sb.toString();
	}

	@Override
	public String join(String... values) {
		return StringUtils.join(values, "");
	}

}
