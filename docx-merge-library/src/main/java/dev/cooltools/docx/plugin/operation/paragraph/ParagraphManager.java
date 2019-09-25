package dev.cooltools.docx.plugin.operation.paragraph;

import java.util.HashMap;

import org.docx4j.wml.P;
import org.jvnet.jaxb2_commons.ppp.Child;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.NopeEvaluationContext;

public class ParagraphManager {
	private Context context;
	private NopeEvaluationContext nopeEvaluationContext;
	
	private boolean hideParagraph;
	private P currentParagraph;
	
	public ParagraphManager(Context context) {
		this.context = context;
		
		nopeEvaluationContext = new NopeEvaluationContext(new HashMap<String, Object>());
		
		context.getDocumentCrawler().subscribe(DocxCrawler.StartParagraph, p -> currentParagraph = p);
		context.getDocumentCrawler().subscribe(DocxCrawler.EndParagraph, p -> hideParagraph(false));
	}

	public void hideParagraph(boolean hide) {
		if (hide) {
			currentParagraph.getContent().stream()
			  .filter(o -> o instanceof Child)
			  .map(o -> (Child) o)
			  .forEach(c -> context.getPostProcessor().registerRemoveElement(c));

			context.getExpressionEvaluator().registerTemporaryContext(nopeEvaluationContext);
		} else {
			if (hideParagraph) {
				context.getExpressionEvaluator().restoreContext();
			}
		}
		hideParagraph = hide;
	}
}
