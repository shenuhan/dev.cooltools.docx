package dev.cooltools.docx.plugin.operation.paragraph;

import java.util.Collection;

public interface ParagraphOperations {
	/**
	 * show the paragraph if value is true
	 * 
	 * @param show : if true, we should show the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean show(boolean show);

	/**
	 * hide the paragraph if value is true
	 * 
	 * @param hide : if true we should hide the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean hide(boolean hide);

	/**
	 * hide the paragraph
	 * 
	 * @return true so you can put multiple operation
	 */
	public boolean hide();

	/**
	 * repeat the paragraph once for every item in objects. It will create a
	 * variable named variableName to access data in every object inside the repeat.
	 * 
	 * @param objects      : the list of objects on which to repeat
	 * @param variableName : the name of the variable in the repeat
	 * @return true so you can put multiple operation
	 */
	public boolean repeat(Collection<Object> objects, String variableName);

}
