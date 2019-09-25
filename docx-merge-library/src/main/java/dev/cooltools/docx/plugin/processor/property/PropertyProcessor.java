package dev.cooltools.docx.plugin.processor.property;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.EvaluationRequester;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.plugin.processor.property.PropertyManager.PropertyWrapper;
import dev.cooltools.docx.util.RunUtil;

public class PropertyProcessor {
	static private Logger log = LoggerFactory.getLogger(PropertyProcessor.class);
	
	private final PropertyManager propertyManager;
	private final Context context;
	
	public PropertyProcessor(Context context) {
		this.propertyManager = new PropertyManager(context);
		this.context = context;
		
		context.getDocumentCrawler().subscribe(DocxCrawler.ProcessRun, this::processNextRun);
		context.getDocumentCrawler().subscribe(DocxCrawler.ProcessCTSimpleField, this::processNextCTSimpleField);
	}

	public void processNextRun(R run) {
		if (run == null)
			return;

		PropertyWrapper property = propertyManager.getPropertyWrapper(run);

		if (property != null) {
			try {
				Object o = context.getExpressionEvaluator().evaluate(EvaluationRequester.DocxProperty, property.getProperty());
				if (o instanceof String || o == null) {
					display(run, o == null ? "" : o.toString());
				}
			} catch (SpelEvaluationException e) {
				log.error("Could not process property " + property.getProperty(), e);
				this.context.getProblemReporter().reportError(ErrorType.ParseProperty, property.getProperty(), e.getLocalizedMessage());
			} catch (Exception e) {
				log.error("Could not process property " + property.getProperty(), e);
				this.context.getProblemReporter().reportError(ErrorType.ParseProperty, property.getProperty());
			}
		}
	}

	public void processNextCTSimpleField(CTSimpleField ctSimpleField) {
		PropertyWrapper property = this.propertyManager.getPropertyWrapper(ctSimpleField);
		try {
			Object o = this.context.getExpressionEvaluator().evaluate(EvaluationRequester.DocxProperty, property.getProperty());
			if (o instanceof String || o == null) {
				context.getPostProcessor().registerRemoveElement(ctSimpleField);
				context.getPostProcessor().registerAddElement(ctSimpleField, RunUtil.create(o == null ? (String) o : o.toString()));
			}
		} catch (SpelEvaluationException e) {
			log.error("Could not process property " + property.getProperty(), e);
			this.context.getProblemReporter().reportError(ErrorType.ParseProperty, property.getProperty(), e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("Could not process property " + property.getProperty(), e);
			this.context.getProblemReporter().reportError(ErrorType.ParseProperty, property.getProperty());
		}
	}

	private void display(R run, String text) {
		RunUtil.setText(run, text);
		boolean remove = false;
		for (Object o : ((ContentAccessor) run.getParent()).getContent()) {
			if (remove) {
				if (o instanceof R) {
					R r = (R) o;
					this.context.getPostProcessor().registerRemoveElement(r);
					// if it is a property replace we check if it is the end
					if (!r.getContent().isEmpty() && XmlUtils.unwrap(r.getContent().get(0)) instanceof FldChar && ((FldChar) XmlUtils.unwrap(r.getContent().get(0))).getFldCharType() == STFldCharType.END) {
						break;
					}
				}
			}
			if (o == run) {
				remove = true;
			}
		}
	}

}
