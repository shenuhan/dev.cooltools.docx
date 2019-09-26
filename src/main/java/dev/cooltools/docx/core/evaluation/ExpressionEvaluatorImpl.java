package dev.cooltools.docx.core.evaluation;

import java.util.Map;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class ExpressionEvaluatorImpl implements ExpressionEvaluator {
	/**
	 * Parsing fields
	 */
	private final DelegatingEvaluationContext safeContext;
	private final SpelExpressionParser parser;

	private Evaluation lastEvaluatedExpression;

	public ExpressionEvaluatorImpl(Map<String, Object> operations, Map<String, Object> variables) {
		this.safeContext = new DelegatingEvaluationContext(new SafeEvaluationContext(operations, variables));
		SpelParserConfiguration config = new SpelParserConfiguration();
		parser = new SpelExpressionParser(config);
	}

	@Override
	public Object evaluate(EvaluationRequester requester, String expression) {
		this.lastEvaluatedExpression = EvaluatorHelper.createEvaluation(expression, requester);
		expression = EvaluatorHelper.replaceQuote(expression);
		return parser.parseExpression(expression).getValue(this.safeContext);
	}

	@Override
	public void addVariable(String variable, Object value) {
		this.safeContext.setVariable(variable, value);
	}

	@Override
	public void removeVariable(String variable) {
		this.safeContext.setVariable(variable, null);
	}

	@Override
	public boolean existsVariable(String name) {
		return this.safeContext.lookupVariable(name) != null;
	}

	@Override
	public void registerTemporaryContext(EvaluationContext temporaryContext) {
		this.safeContext.setTemporaryEvaluationContext(temporaryContext);
	}

	@Override
	public void restoreContext() {
		this.safeContext.restoreEvaluationContext();
	}

	@Override
	public Evaluation getLastEvaluatedExpression() {
		return this.lastEvaluatedExpression;
	}
}
