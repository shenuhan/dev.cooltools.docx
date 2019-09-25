package dev.cooltools.docx.core;

import java.util.List;

import org.jvnet.jaxb2_commons.ppp.Child;

public interface PostProcessor {
	static enum Priority {
		High, Standard, Low
	}
	
	void registerPostProcessingOperation(Runnable runnable);

	void registerPostProcessingOperation(Runnable runnable, Priority p);

	void registerRemoveElement(Child remove);

	void registerAddElements(Child after, List<? extends Child> objects);

	void registerAddElement(Child after, Child object);
}