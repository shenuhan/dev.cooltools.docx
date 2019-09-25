package dev.cooltools.docx.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import dev.cooltools.docx.core.evaluation.ExpressionEvaluator;
import dev.cooltools.docx.error.ProblemReporter;

public interface Context {
	/**
	 * This is the current document being evaluated
	 * 
	 * @return the document
	 */
	WordprocessingMLPackage getDocument();

	/**
	 * This is the current evaluator for spel expression
	 * 
	 * @return the evaluator
	 */
	ExpressionEvaluator getExpressionEvaluator();

	/**
	 * This is the main processor it will read the document and trigger all the
	 * RunProcessor
	 * 
	 * @return The main processor
	 */
	DocxCrawler getDocumentCrawler();

	/**
	 * This Processor allows you to register actions to do after the document
	 * processing is over. Mainly it will add loop elements and remove hidden ones
	 * and comments
	 * 
	 * @return the docuemnt post processor
	 */
	PostProcessor getPostProcessor();

	/**
	 * This is a helper to reporty problem or warning during the evaluation
	 * 
	 * @return the document problem reporter
	 */
	ProblemReporter getProblemReporter();
}
