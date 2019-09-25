package dev.cooltools.docx.plugin.processor.inline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.EvaluationRequester;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.util.RunUtil;

public class InlineProcessor {
	private static final Logger log = LoggerFactory.getLogger(InlineProcessor.class);

	private static final Pattern inlinestart = Pattern.compile("\\$\\{([^\\}]*)(\\}?)");
	private static final Pattern inlineend = Pattern.compile("(.*?)\\}");

	private String inlinestarted = null;
	private R previous$ = null;

	private Context context;
	
	public InlineProcessor(Context context) {
		this.context = context;
		context.getDocumentCrawler().subscribe(DocxCrawler.ProcessRun, this::processNextRun);
	}

	public void processNextRun(R run) {
		if (inlinestarted == null) {
			processInlineStart(run);
		} else {
			processInlineEnd(run);
		}
	}

	private void processInlineStart(R run) {
		String runString = previous$ == null ? RunUtil.getText(run) : ("$" + RunUtil.getText(run));
		Matcher m = inlinestart.matcher(runString);
		StringBuffer buffer = new StringBuffer();
		if (m.find()) {
			if (previous$ != null) {
				// we remove the $ from the previous run
				RunUtil.setText(previous$, RunUtil.getText(previous$).replaceFirst("\\$$", ""));
				previous$ = null;
			}
			do {
				if (m.group(2).isEmpty()) {
					// this means the inline is not finished in this run
					inlinestarted = m.group(1);
					m.appendReplacement(buffer, "");
					break;
				} else {
					String value = m.group(1);
					try {
						Object o = this.context.getExpressionEvaluator().evaluate(EvaluationRequester.Inline, value);
						if (o instanceof String) {
							m.appendReplacement(buffer, o.toString());
						} else if (o == null || o instanceof Boolean) {
							m.appendReplacement(buffer, "");
						}
					} catch (SpelEvaluationException e) {
						log.error("Could not process inline " + m.group(1), e);
						this.context.getProblemReporter().reportError(ErrorType.ParseInline, value, e.getLocalizedMessage());
					} catch (Exception e) {
						log.error("Could not process inline " + m.group(1), e);
						this.context.getProblemReporter().reportError(ErrorType.ParseInline, value);
					}
				}
			} while ((m.find()));
			m.appendTail(buffer);
			RunUtil.setText(run, buffer.toString());
		} else {
			if (runString.endsWith("$")) {
				previous$ = run;
			} else {
				previous$ = null;
			}
		}
	}

	private void processInlineEnd(R run) {
		String runString = RunUtil.getText(run);
		Matcher m = inlineend.matcher(runString);
		StringBuffer buffer = new StringBuffer();
		if (m.find()) {
			try {
				inlinestarted += m.group(1);
				Object o = this.context.getExpressionEvaluator().evaluate(EvaluationRequester.Inline, inlinestarted);
				if (o instanceof String) {
					m.appendReplacement(buffer, o.toString());
				} else if (o == null || o instanceof Boolean) {
					m.appendReplacement(buffer, "");
				}
				m.appendTail(buffer);
				RunUtil.setText(run, buffer.toString());
			} catch (SpelEvaluationException e) {
				log.error("Could not process inline multi run " + inlinestarted, e);
				this.context.getProblemReporter().reportError(ErrorType.ParseInline, inlinestarted, e.getLocalizedMessage());
			} catch (Exception e) {
				log.error("Could not process inline multi run " + inlinestarted, e);
				this.context.getProblemReporter().reportError(ErrorType.ParseInline, inlinestarted);
			} finally {
				inlinestarted = null;
			}

			processInlineStart(run);
		} else {
			inlinestarted += runString;
			RunUtil.setText(run, "");
		}
	}

}
