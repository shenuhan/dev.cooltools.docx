package dev.cooltools.docx.error;

public class DocxProcessingException extends Exception {
	private static final long serialVersionUID = 1L;
	private ProblemReporter reporter;

	DocxProcessingException(ProblemReporter reporter) {
		this.reporter = reporter;
	}

	public ProblemReporter getReporter() {
		return reporter;
	}
}