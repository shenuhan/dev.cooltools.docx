package dev.cooltools.docx.service;

import java.util.List;
import java.util.ResourceBundle;

public interface Property {
	public enum VariableType {
		Unknown, Object, Number, String, Date, Method, List, Boolean;

		public String getLocalizedMessage() {
			ResourceBundle messages = ResourceBundle.getBundle("i18n.enum.VariableType");
			return messages.getString(name());
		}
	}

	String getName();

	VariableType getType();

	List<Property> getProperties();

	Property addProperty(String name, VariableType type);

	void setType(VariableType method);
}
