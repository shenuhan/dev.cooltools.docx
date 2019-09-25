package dev.cooltools.docx.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;

import dev.cooltools.docx.core.PostProcessor;

public class DocumentPostProcessorImpl implements PostProcessor {
	private WordprocessingMLPackage document;
	private Priority runningPostProcessing = null;

	private Map<Priority, List<Runnable>> runnables = new HashMap<PostProcessor.Priority, List<Runnable>>();

	public DocumentPostProcessorImpl(WordprocessingMLPackage document) {
		this.document = document;
		for (Priority p : Priority.values()) runnables.put(p, new ArrayList<Runnable>());
	}
	
	@Override
	public void registerPostProcessingOperation(Runnable runnable) {
		registerPostProcessingOperation(runnable, Priority.Standard);
	}
	
	@Override
	public void registerPostProcessingOperation(Runnable runnable, Priority p) {
		if (runningPostProcessing != null && p.ordinal() <= runningPostProcessing.ordinal()) {
			runnable.run();
		} else {
			this.runnables.get(p).add(runnable);
		}
	}

	@Override
	public void registerRemoveElement(Child remove) {
		this.registerPostProcessingOperation(() -> removeElement(remove), Priority.Low);
	}

	@Override
	public void registerAddElements(Child after, List<? extends Child> objects) {
		this.registerPostProcessingOperation(() -> addElements(after, objects), Priority.High);
	}

	@Override
	public void registerAddElement(Child after, Child object) {
		this.registerPostProcessingOperation(() -> addElements(after, List.of(object)), Priority.High);
	}

	private void addElements(Child after, List<? extends Child> objects) {
		Object parent = after.getParent();
		if (!(parent instanceof ContentAccessor)) {
			return;
		}
		List<Object> content = ((ContentAccessor) parent).getContent();
		int pos = content.indexOf(after);
		if (pos >= 0) {
			objects.forEach(o -> (o).setParent(parent));
			content.addAll(pos + 1, objects);
		} else {
			// we didnt find the after object might be it is wrapper
			for (int i = 0; i < content.size(); i++) {
				if (XmlUtils.unwrap(content.get(i)) == after) {
					content.addAll(i + 1, objects);
					break;
				}
			}
		}
	}

	private void removeElement(Child remove) {
		if (!(remove.getParent() instanceof ContentAccessor)) {
			return;
		}
		List<Object> content = ((ContentAccessor) ((Child) remove).getParent()).getContent();
		for (int i = 0; i < content.size(); i++) {
			if (XmlUtils.unwrap(content.get(i)) == remove) {
				Object parent = ((Child) remove).getParent();
				content.remove(i);
				if (content.isEmpty()) {
					document.getMainDocumentPart().getContent().remove(parent);
				}
				break;
			}
		}
	}

	public void execute() {
		for (Priority p : Priority.values()) {
			runningPostProcessing = p;
			runnables.get(p).stream().forEach(r -> r.run());
		}
	}
}
