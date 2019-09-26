package dev.cooltools.docx.plugin.processor;

import dev.cooltools.docx.plugin.Plugin;

/**
 * The ProcessorPlugin should be the one reading the comment or properties to evaluate them.
 * It will find for exemple the Comment or properties which have relevant expressions to be evaluated
 * @author jean
 *
 */
public interface ProcessorPlugin<BeanType> extends Plugin<BeanType> {
}
