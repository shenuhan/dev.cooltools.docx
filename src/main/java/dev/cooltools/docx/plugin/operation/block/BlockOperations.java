package dev.cooltools.docx.plugin.operation.block;

import java.util.Collection;

import dev.cooltools.docx.plugin.operation.Operations;

public interface BlockOperations extends Operations {
	/**
	 * show the block if value is true
	 * 
	 * @param show : if true, we should show the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean show(boolean show);

	/**
	 * hide the block if value is true
	 * 
	 * @param hide : if true we should hide the paragraph
	 * @return true so you can put multiple operation
	 */
	public boolean hide(boolean hide);

	/**
	 * hide the block
	 * 
	 * @return true so you can put multiple operation
	 */
	public boolean hide();

	/**
	 * repeat all the paragraphs in the block once for every item in objects. It
	 * will create a variable named variableName to access data in every object
	 * inside the repeat.
	 * 
	 * @param objects      : the list of objects on which to repeat
	 * @param variableName : the name of the variable in the repeat
	 * @return true so you can put multiple operation
	 */
	public boolean repeat(Collection<Object> objects, String variableName);

	/**
	 * all block should end, otherwise there will be an error
	 * 
	 * @return true so you can put multiple operation
	 */
	public boolean end();
}
