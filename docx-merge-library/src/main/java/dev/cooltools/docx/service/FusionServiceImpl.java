package dev.cooltools.docx.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dev.cooltools.docx.DocxFusion;
import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.plugin.LoopManagerPlugin;
import dev.cooltools.docx.plugin.Plugin;
import dev.cooltools.docx.plugin.operation.StringUtilsOperationPlugin;
import dev.cooltools.docx.plugin.operation.block.BlockManagerPlugin;
import dev.cooltools.docx.plugin.operation.block.BlockOperationPlugin;
import dev.cooltools.docx.plugin.operation.general.GeneralOperationPlugin;
import dev.cooltools.docx.plugin.operation.paragraph.ParagraphOperationPlugin;
import dev.cooltools.docx.plugin.operation.row.RowOperationPlugin;
import dev.cooltools.docx.plugin.operation.text.TextOperationPlugin;
import dev.cooltools.docx.plugin.processor.MultiRunManagerPlugin;
import dev.cooltools.docx.plugin.processor.comment.CommentProcessorPlugin;
import dev.cooltools.docx.plugin.processor.inline.InlineProcessorPlugin;
import dev.cooltools.docx.plugin.processor.property.PropertyProcessorPlugin;

public class FusionServiceImpl implements FusionService {
	private final List<Plugin<?>> plugins = new LinkedList<Plugin<?>>();
	private final List<Plugin<?>> defaultPlugins = Arrays.asList(
		new MultiRunManagerPlugin(),
		
		new BlockOperationPlugin(),
		new GeneralOperationPlugin(),
		new ParagraphOperationPlugin(),
		new RowOperationPlugin(),
		new StringUtilsOperationPlugin(),
		new TextOperationPlugin(),
		
		new CommentProcessorPlugin(),
		new InlineProcessorPlugin(),
		new PropertyProcessorPlugin(),
		
		new LoopManagerPlugin(),
		new BlockManagerPlugin()
			);
	
	public void merge(InputStream file, OutputStream out, final Map<String, Object> context) throws DocxProcessingException {
		var fusion = new DocxFusion(file, context);
		fusion.instanciatePlugins(plugins.isEmpty() ? defaultPlugins : plugins);
		fusion.processDocument();
		fusion.save(out);
	}

	public List<Property> findAllVariables(InputStream file) throws DocxProcessingException {
		List<Property> properties = new ArrayList<Property>();

		var fusion = new DocxFusion(file, properties);
		fusion.instanciatePlugins(plugins.isEmpty() ? defaultPlugins : plugins);
		fusion.processDocument();
		
		return properties;
	}

	public void registerPlugin(Plugin<?>...plugins) {
		this.plugins.addAll(Arrays.asList(plugins));
	}
}
