package dev.cooltools.docx.plugin.operation.general;

public interface GeneralOperations {
	/**
	 * Will replace the part with the given text
	 * 
	 * @param name:  the name of the variable we will create
	 * @param value: the value to be put in place
	 * @return true so you can put multiple operation
	 */
	public boolean set(String name, Object value);
}
