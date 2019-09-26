package dev.cooltools.docx.error;

import java.util.ResourceBundle;

public enum ErrorType {
	DocumentInitializing, // An error occurred while initializing document.
	SaveDocument, // An error occurred while saving the document.

	ParseComment, // There was an error while parsing comment.
	ParseInline, // There was an error while parsing an in-line string.
	ParseProperty, // There was an error while parsing a property.

	ExistingVariable, // The variable %s already exists.
	VariableNameNotEmpty, // The name of the variable for a loop canot be null.
	MultipleRowLoop, // You cannot do two row repeat in the same row.
	EndBlockWithNoStart, // A block ended with no corresponding show/hide/repeat.
	InconcistentType // The property '%s' is detected for two incompatible types: '%s' and '%s'.
	;

	public String getLocalizedMessage(Object... args) {
		ResourceBundle messages = ResourceBundle.getBundle("i18n.enum.ErrorType");
		return String.format(messages.getString(name()), args);
	}
}