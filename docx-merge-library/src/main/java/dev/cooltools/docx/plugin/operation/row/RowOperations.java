package dev.cooltools.docx.plugin.operation.row;

import java.util.Collection;

public interface RowOperations {
	/**
	 * show the row if value is true
	 * 
	 * @param show : if true, we should show the row
	 * @return true so you can put multiple operation
	 */
	public boolean show(boolean show);

	/**
	 * hide the row if value is true
	 * 
	 * @param hide : if true we should hide the row
	 * @return true so you can put multiple operation
	 */
	public boolean hide(boolean hide);

	/**
	 * hide the row
	 * 
	 * @return true so you can put multiple operation
	 */
	public boolean hide();

	/**
	 * repeat the row in the table once for every item in objects. It will create a
	 * variable named variableName to access data in every object inside the repeat.
	 * 
	 * @param objects      : the list of objects on which to repeat
	 * @param variableName : the name of the variable in the repeat
	 * @return true so you can put multiple operation
	 */
	public boolean repeat(Collection<Object> objects, String variableName);
}
