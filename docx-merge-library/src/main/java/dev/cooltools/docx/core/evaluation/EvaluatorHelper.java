package dev.cooltools.docx.core.evaluation;

import java.util.ResourceBundle;

import org.apache.commons.lang3.RegExUtils;

import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.Evaluation;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.EvaluationRequester;

public class EvaluatorHelper {
	static private class EvaluationImpl implements Evaluation {
		private final String expression;
		private final EvaluationRequester requester;

		public EvaluationImpl(String expression, EvaluationRequester requester) {
			this.expression = expression;
			this.requester = requester;
		}

		@Override
		public String getExpression() {
			return expression;
		}

		@Override
		public EvaluationRequester getRequester() {
			return requester;
		}

		@Override
		public String toString() {
			return String.format("%s(%s)", requester, expression);
		}

		@Override
		public String toLocalizedString() {
			ResourceBundle messages = ResourceBundle.getBundle("i18n.errors.errors");
			return String.format(messages.getString("evaluation.information"), requester.getLocalizedMessage(), expression);
		}
	}

	/**
	 * Replace the locale docx quote by the standard ' quote
	 * 
	 * @param expression
	 * @return the string with the replaced quotes
	 */
	public static String replaceQuote(String expression) {
		return RegExUtils.replaceAll(expression, "[`‘‚’]", "'");
	}

	public static Evaluation createEvaluation(String expression, EvaluationRequester requester) {
		return new EvaluationImpl(expression, requester);
	}

}
