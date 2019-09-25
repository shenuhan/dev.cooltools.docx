package dev.cooltools.docx.service;

public class FusionException extends Exception {
	private static final long serialVersionUID = 1L;

	public FusionException(String message, Throwable t) {
		super(message, t);
	}

	public FusionException(String message) {
		super(message);
	}
}
