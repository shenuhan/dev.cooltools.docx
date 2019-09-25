package dev.cooltools.docx.plugin.processor.comment;

import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.EvaluationRequester;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.processor.MultiRunManager;
import dev.cooltools.docx.plugin.processor.comment.CommentManager.CommentWrapper;
import dev.cooltools.docx.util.RunUtil;

public class CommentProcessor {
	static private Logger log = LoggerFactory.getLogger(CommentProcessor.class);
	
	private CommentManager commentManager;
	private Context context;
	
	@InjectPluginBean
	private MultiRunManager multiRunManager;
	
	public CommentProcessor(Context context) {
		this.commentManager = new CommentManager(context);
		this.context = context;
		
		context.getDocumentCrawler().subscribe(DocxCrawler.ProcessRun, run -> processNextRun(run));
	}

	private void processNextRun(R run) {
		if (run == null)
			return;

		CommentWrapper comment = commentManager.getComment(run);

		if (comment != null) {
			this.context.getPostProcessor().registerPostProcessingOperation(() -> CommentManager.deleteComment(comment));

			multiRunManager.addRun(run);
			if (comment.getLastRun() == run) {
				String value = CommentManager.getCommentString(comment.getComment());
				try {
					Object o = context.getExpressionEvaluator().evaluate(EvaluationRequester.Comment, value);
					if (o instanceof String) {
						display(run, o == null ? "" : o.toString());
					} else if (o == null) {
						display(run, "");
					} else if (o instanceof Number) {
						display(run, o.toString());
					}
				} catch (SpelEvaluationException e) {
					log.error("Could not process comment " + value, e);
					this.context.getProblemReporter().reportError(ErrorType.ParseComment, value, e.getLocalizedMessage());
				} catch (Exception e) {
					log.error("Could not process comment " + value, e);
					this.context.getProblemReporter().reportError(ErrorType.ParseComment, value);
				} finally {
					this.multiRunManager.clearRuns();
				}
			}
		}
	}

	private void display(R run, String text) {
		RunUtil.setText(run, text);
		for (R o : this.multiRunManager.getRuns()) {
			if (o != run) {
				context.getPostProcessor().registerRemoveElement(o);
			}
		}
	}
}
