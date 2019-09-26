package dev.cooltools.docx.core.evaluation;

import java.util.function.Supplier;

import org.springframework.expression.TypedValue;

public class RootObjectDelegator {

	private final Supplier<TypedValue> rootObjectProvider;

	public RootObjectDelegator(Supplier<TypedValue> rootObjectProvider) {
		this.rootObjectProvider = rootObjectProvider;
	}

	public Supplier<TypedValue> getRootObjectProvider() {
		return rootObjectProvider;
	}

}
