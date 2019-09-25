package dev.cooltools.docx.core.evaluation;

import java.util.ResourceBundle;

import org.springframework.expression.EvaluationContext;

/**
 * This will manage the access to the evaluation context. It can evaluate
 * expression and add or remove variables.
 * 
 * @author jean
 *
 */
public interface ExpressionEvaluator {
	static public enum EvaluationRequester {
		Comment, Inline, DocxProperty;

		public String getLocalizedMessage() {
			ResourceBundle messages = ResourceBundle.getBundle("i18n.enum.EvaluationRequester");
			return messages.getString(name());
		}
	}

	static public interface Evaluation {
		String getExpression();

		EvaluationRequester getRequester();

		String toLocalizedString();
	}

	/**
	 * Evaluate a spel expression against the current context
	 * 
	 * @param requester:  to be able to store where the evaluation was coming from
	 *                    in case of error (comment, inline or property)
	 * @param expression: The expression to evaluate
	 * @return the result, could be a String to display or a boolean if this is a
	 *         function of Operations
	 */
	Object evaluate(EvaluationRequester requester, String expression);

	/**
	 * This will add a variable to the current evaluation context
	 * 
	 * @param variable name of the variable
	 * @param value    value of the variable
	 */
	void addVariable(String variable, Object value);

	/**
	 * This will remove a variable from the current evaluation context
	 * 
	 * @param variable name of the variable
	 */
	void removeVariable(String variable);

	/**
	 * Check if a variable exists
	 * 
	 * @param name name of the variable
	 * @return true if it already exists
	 */
	boolean existsVariable(String name);

	/**
	 * this register a temporary Context that will be used until restore context is
	 * called This is usefull when there is a p.hide or a block.hide or empty loop
	 * to not evaluate until the paragraph or block is over
	 * 
	 * @param temporaryContext : the temporary context
	 */
	void registerTemporaryContext(EvaluationContext temporaryContext);

	/**
	 * This restores the main context
	 */
	void restoreContext();

	/**
	 * This restores the main context
	 * 
	 * @return the text that is currently being evaluated
	 */
	Evaluation getLastEvaluatedExpression();
}
