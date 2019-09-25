package dev.cooltools.docx.error;

import java.util.List;

public interface ProblemReporter {
	/**
	 * To get all errors that were reported during processing
	 * 
	 * @return List of error
	 */
	List<PublicationError> getErrors();

	/**
	 * To report an error that has a compicated description
	 * 
	 * @param errorType   type of error
	 * @param description description
	 * @param args        other things that might be usefull for reporting the error
	 */
	void reportError(ErrorType errorType, String description, Object... args);

	/**
	 * Report a simple error
	 * 
	 * @param errorType
	 */
	void reportError(ErrorType errorType);

	/**
	 * Does the reporter has any errors to report
	 * 
	 * @return true if there are errors
	 */
	boolean hasErrors();

	/**
	 * Throw an exception if there are any errors
	 * 
	 * @throws DocxProcessingException
	 */
	void throwException() throws DocxProcessingException;
}