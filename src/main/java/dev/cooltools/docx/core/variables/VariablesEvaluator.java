package dev.cooltools.docx.core.variables;

import java.util.List;
import java.util.Map;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import dev.cooltools.docx.core.evaluation.EvaluatorHelper;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator;
import dev.cooltools.docx.error.ProblemReporter;
import dev.cooltools.docx.service.Property;

public class VariablesEvaluator implements ExpressionEvaluator {
	/**
	 * Parsing fields
	 */
	private final VariableGetterEvaluationContext safeContext;
	private final SpelExpressionParser parser;

	private Evaluation lastEvaluatedExpression;

	public VariablesEvaluator(Map<String, Object> operations, List<Property> properties, ProblemReporter problemReporter) {
		SpelParserConfiguration config = new SpelParserConfiguration();
		parser = new SpelExpressionParser(config);
		this.safeContext = new VariableGetterEvaluationContext(operations, properties, problemReporter);
	}
	
	@Override
	public Object evaluate(EvaluationRequester requester, String expression) {
		this.lastEvaluatedExpression = EvaluatorHelper.createEvaluation(expression, requester);
		expression = EvaluatorHelper.replaceQuote(expression);
		return parser.parseExpression(expression).getValue(this.safeContext, String.class);
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
		return this.safeContext.existsVariable(name);
	}

	@Override
	public void registerTemporaryContext(EvaluationContext temporaryContext) {
	}

	@Override
	public void restoreContext() {
	}

	@Override
	public Evaluation getLastEvaluatedExpression() {
		return this.lastEvaluatedExpression;
	}
}
