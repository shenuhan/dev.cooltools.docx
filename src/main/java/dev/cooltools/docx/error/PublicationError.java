package dev.cooltools.docx.error;

import dev.cooltools.docx.core.evaluation.ExpressionEvaluator.Evaluation;

public interface PublicationError {

	ErrorType getErrorType();

	Evaluation getEvaluation();

	String getLocalizedErrorMessage();

}