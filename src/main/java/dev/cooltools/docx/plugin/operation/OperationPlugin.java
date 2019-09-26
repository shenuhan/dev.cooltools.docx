package dev.cooltools.docx.plugin.operation;

import dev.cooltools.docx.plugin.Plugin;

/**
 * The Operation Plugin can enhance the operations available in the comments or property ...
 * It adds the Bean returned by bean to the available operations with the each of the given prefix 
 * @author jean
 *
 */
public interface OperationPlugin<BeanType> extends Plugin<BeanType> {
	/**
	 * The prefix for the operation
	 * @return
	 */
	String[] prefixes();
}
