package dev.cooltools.docx.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import dev.cooltools.docx.error.DocxProcessingException;

public interface FusionService {
	void merge(InputStream file, OutputStream out, final Map<String, Object> context) throws DocxProcessingException;

	List<Property> findAllVariables(InputStream file) throws DocxProcessingException;
}
