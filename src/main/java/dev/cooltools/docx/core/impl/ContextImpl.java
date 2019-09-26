package dev.cooltools.docx.core.impl;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.PostProcessor;
import dev.cooltools.docx.core.evaluation.ExpressionEvaluator;
import dev.cooltools.docx.error.ProblemReporter;

public class ContextImpl implements Context {
	private final DocxCrawler documentCrawler;
	private final PostProcessor postProcessor;
	private final ProblemReporter problemReporter;
	private final ExpressionEvaluator expressionEvaluator;
	private final WordprocessingMLPackage document;

	public ContextImpl(
			WordprocessingMLPackage document, 
			DocxCrawler documentProcessor, 
			ExpressionEvaluator evaluator, 
			PostProcessor postProcessor, 
			ProblemReporter problemReporter) throws Docx4JException {
		this.document = document;
		this.documentCrawler = documentProcessor;
		this.postProcessor = postProcessor;
		this.problemReporter = problemReporter;
		this.expressionEvaluator = evaluator;
	}

	@Override
	public DocxCrawler getDocumentCrawler() {
		return this.documentCrawler;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		return expressionEvaluator;
	}

	@Override
	public PostProcessor getPostProcessor() {
		return postProcessor;
	}

	@Override
	public ProblemReporter getProblemReporter() {
		return problemReporter;
	}

	@Override
	public WordprocessingMLPackage getDocument() {
		return this.document;
	}
}
