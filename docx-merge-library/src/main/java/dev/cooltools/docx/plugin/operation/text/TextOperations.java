package dev.cooltools.docx.plugin.operation.text;

import java.util.Collection;

public interface TextOperations {
	/**
	 * Will replace the part with the given text
	 * 
	 * @param value: the value to be put in place
	 * @return true so you can put multiple operation
	 */
	public boolean value(String value);

	/**
	 * This will join the given strings
	 * 
	 * @param values: the strings to join
	 * @return the joined string
	 */
	public String join(Collection<String> values);

	/**
	 * This will join the given strings with the given separator
	 * 
	 * @param values:   the strings to join
	 * @param separator : the separator to use
	 * @return the joined string
	 */
	public String join(Collection<String> values, String separator);

	/**
	 * This will join the given strings with the given separator
	 * 
	 * @param values:       the strings to join
	 * @param separator     : the separator to use
	 * @param lastSeparator : the last separator
	 * @return the joined string
	 */
	public String join(Collection<String> values, String separator, String lastSeparator);

	/**
	 * This will join the given strings
	 * 
	 * @param values: the strings to join
	 * @return the joined string
	 */
	public String join(String... values);

	/**
	 * show the text if value is true
	 * 
	 * @param show : if true, we should show the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean show(boolean show);

	/**
	 * hide the text if value is true
	 * 
	 * @param hide : if true we should hide the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean hide(boolean hide);

	/**
	 * hide the text
	 * 
	 * @return true so you can put multiple operation
	 */
	public boolean hide();
}
