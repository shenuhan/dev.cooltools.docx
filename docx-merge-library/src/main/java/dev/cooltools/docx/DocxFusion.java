package dev.cooltools.docx;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluatorImpl;
import dev.cooltools.docx.core.impl.ContextImpl;
import dev.cooltools.docx.core.impl.DocumentPostProcessorImpl;
import dev.cooltools.docx.core.impl.DocxCrawlerImpl;
import dev.cooltools.docx.core.variables.VariablesEvaluator;
import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.error.ProblemReporterImpl;
import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.Plugin;
import dev.cooltools.docx.plugin.operation.Initialize;
import dev.cooltools.docx.plugin.operation.OperationPlugin;
import dev.cooltools.docx.plugin.operation.Operations;
import dev.cooltools.docx.service.Property;

public class DocxFusion {
	static private Logger log = LoggerFactory.getLogger(DocxFusion.class);

	private Context context;
	private DocxCrawlerImpl documentCrawler;
	private DocumentPostProcessorImpl postProcessor;
	
	private Map<String, Object> operations = new HashMap<String, Object>();

	public DocxFusion(InputStream templateDocx, Map<String, Object> variables) throws DocxProcessingException {
		try {
			WordprocessingMLPackage document = WordprocessingMLPackage.load(templateDocx);
			documentCrawler = new DocxCrawlerImpl(document);
			ProblemReporterImpl problemReporter = new ProblemReporterImpl();
			ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl(operations, variables);
			postProcessor = new DocumentPostProcessorImpl(document);
			
			context = new ContextImpl(document, documentCrawler, evaluator, postProcessor, problemReporter);
			problemReporter.registerContext(context);
		} catch (Docx4JException e) {
			log.error("An error occured while initializing document", e);
			ProblemReporterImpl.reportAndThrow(ErrorType.DocumentInitializing, e.getLocalizedMessage());
		}
	}

	public DocxFusion(InputStream templateDocx, List<Property> properties) throws DocxProcessingException {
		try {
			WordprocessingMLPackage document = WordprocessingMLPackage.load(templateDocx);
			documentCrawler = new DocxCrawlerImpl(document);
			ProblemReporterImpl problemReporter = new ProblemReporterImpl();
			VariablesEvaluator evaluator = new VariablesEvaluator(operations, properties, problemReporter);
			postProcessor = new DocumentPostProcessorImpl(document);
			
			context = new ContextImpl(document, documentCrawler, evaluator, postProcessor, problemReporter);
			problemReporter.registerContext(context);
		} catch (Docx4JException e) {
			log.error("An error occured while initializing document", e);
			ProblemReporterImpl.reportAndThrow(ErrorType.DocumentInitializing, e.getLocalizedMessage());
		}
	}
	
	public void processDocument() throws DocxProcessingException {
		if (context == null) {
			throw new RuntimeException("There was a problem during the init, check the problem reporter");
		}
		
		documentCrawler.crawlDocument();
		postProcessor.execute();

		if (context.getProblemReporter().hasErrors()) {
			context.getProblemReporter().throwException();
		}
	}

	public void save(OutputStream out) throws DocxProcessingException {
		try {
			TocGenerator tocGenerator = new TocGenerator(context.getDocument());
			tocGenerator.updateToc(true); // true --> skip page numbering; its currently much faster*/
		} catch (TocException e) {
			// no toc
		} 
		try {
			context.getDocument().save(out);
		} catch (Docx4JException e) {
			log.error("An error occured while initializing document", e);
			ProblemReporterImpl.reportAndThrow(ErrorType.SaveDocument, e.getLocalizedMessage());
		}
		
	}

	public void instanciatePlugins(List<Plugin<?>> plugins) {
		Map<Class<?>, Object> beans = new HashMap<Class<?>, Object>();
		List<Object> listBeans = new LinkedList<Object>();
		for (var plugin : plugins) {
			Object bean = plugin.bean(context);
			if (plugin instanceof OperationPlugin) {
				OperationPlugin<?> op = (OperationPlugin<?>)plugin;
				for (var prefix : op.prefixes()) {
					operations.put(prefix, bean);
				}
				if (bean instanceof Operations) {
					((Operations) bean).setPrefixes(op.prefixes());
				}
			}
			listBeans.add(bean);
			Class<?> current = bean.getClass();
			while (current != Object.class) {
				beans.put(current, bean);
				current = current.getSuperclass();
			}
			for (var clazz : bean.getClass().getInterfaces()) {
				beans.put(clazz, bean);
			}
		}
		
		for (var bean : listBeans) {
			for (var field: bean.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(InjectPluginBean.class)) {
					Object inject = beans.get(field.getType());
					if (inject == null) {
						throw new RuntimeException("Cannot find " + field.getType().getName() + " to inject in " + bean.getClass().getName());
					} else {
						try {
							field.setAccessible(true);
							field.set(bean, inject);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException("Cannot inject " + field.getType().getName() + " in " + bean.getClass().getName(), e);
						}
					}
				}
			}
		}
		for (var bean : listBeans) {
			if (bean instanceof Initialize) {
				((Initialize) bean).initialize();
			}
		}
		
	}
}
