package dev.cooltools.docx.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.Evaluation;

public class PublicationErrorImpl implements PublicationError {
	private final ErrorType errorType;
	private final Evaluation evaluation;
	private final String description;
	private final Object[] args;

	public PublicationErrorImpl(ErrorType errorType, Evaluation evaluation) {
		this(errorType, null, evaluation);
	}

	public PublicationErrorImpl(ErrorType errorType, String description, Evaluation evaluation, Object... args) {
		this.errorType = errorType;
		this.description = description;
		this.evaluation = evaluation;
		this.args = args;
	}

	@Override
	public ErrorType getErrorType() {
		return errorType;
	}

	@Override
	public Evaluation getEvaluation() {
		return evaluation;
	}

	public String getDescription() {
		return description;
	}

	public Object[] getArgs() {
		return args;
	}

	@Override
	public String getLocalizedErrorMessage() {
		List<Object> args = new ArrayList<Object>();
		if (description != null)
			args.add(description);

		for (Object o : this.args)
			args.add(o);

		return errorType.getLocalizedMessage(args.toArray());
	}

	@Override
	public String toString() {
		return String.format("[ %s | %s | %s | %s ]", errorType, evaluation != null ? evaluation.toString() : "no evaluation", description != null ? description : "no description",
		        Arrays.asList(args).stream().map(o -> o.toString()).collect(Collectors.joining("-")));
	}
}