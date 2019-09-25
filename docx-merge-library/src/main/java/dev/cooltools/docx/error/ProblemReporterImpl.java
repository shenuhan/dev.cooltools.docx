package dev.cooltools.docx.error;

import java.util.ArrayList;
import java.util.List;

import dev.cooltools.docx.core.Context;

public class ProblemReporterImpl implements ProblemReporter {
	private final List<PublicationError> errors;
	private Context context;

	public ProblemReporterImpl() {
		this.errors = new ArrayList<PublicationError>();
	}
	
	public void registerContext(Context context) {
		this.context = context;
	}

	static public void reportAndThrow(ErrorType errorType, String description) throws DocxProcessingException {
		var reporter = new ProblemReporterImpl();
		reporter.reportError(errorType, description);
		reporter.throwException();
	}

	@Override
	public List<PublicationError> getErrors() {
		return List.copyOf(errors);
	}

	@Override
	public void reportError(ErrorType errorType, String description, Object... args) {
		this.errors.add(new PublicationErrorImpl(errorType, description, context.getExpressionEvaluator().getLastEvaluatedExpression(), args));
	}

	@Override
	public void reportError(ErrorType errorType) {
		this.errors.add(new PublicationErrorImpl(errorType, context.getExpressionEvaluator().getLastEvaluatedExpression()));
	}

	@Override
	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	@Override
	public void throwException() throws DocxProcessingException {
		throw new DocxProcessingException(this);
	}
}
