package dev.cooltools.docx.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.error.ProblemReporter;

public class PropertyImpl implements Property {
	private final String name;
	private VariableType type;
	private final List<Property> properties;
	private final ProblemReporter reporter;

	public PropertyImpl(String name, VariableType type, ProblemReporter reporter) {
		this.name = name;
		this.type = type;
		this.properties = new ArrayList<Property>();
		this.reporter = reporter;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public VariableType getType() {
		return type;
	}

	@Override
	public void setType(VariableType type) {
		if (this.type == VariableType.Unknown) {
			this.type = type;
			return;
		}

		// if it is a Method, we don't set the expected return type
		if (this.type == VariableType.Method) {
			return;
		}
		// if it is a String but was determined to be a Number we keep Number as it can
		// be casted
		if (type == VariableType.String && this.type == VariableType.Number) {
			return;
		}
		// Opposite of the previous reasoning, if it is a String we accept a new Type as
		// Number
		if (this.type == VariableType.String && type == VariableType.Number) {
			this.type = type;
			return;
		}

		if (this.type != type) {
			this.reporter.reportError(ErrorType.InconcistentType, name, type.getLocalizedMessage(), this.type.getLocalizedMessage());
		}
	}

	@Override
	public List<Property> getProperties() {
		return Collections.unmodifiableList(properties);
	}

	@Override
	public Property addProperty(String name, VariableType type) {
		if (this.type != VariableType.List && type != VariableType.Method) {
			setType(VariableType.Object);
		}

		Property p = new PropertyImpl(name, type, reporter);
		properties.add(p);
		return p;
	}
}
