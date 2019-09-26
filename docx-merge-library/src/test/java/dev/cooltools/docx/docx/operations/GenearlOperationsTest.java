package dev.cooltools.docx.docx.operations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.service.FusionServiceFactory;
import dev.cooltools.docx.util.HtmlConverter;

public class GenearlOperationsTest {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void generalOperations() throws Exception {
		var f = File.createTempFile("GeneralOperationsTest", ".docx");
		try (var in = GenearlOperationsTest.class.getResourceAsStream("/general/GeneralOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, Map.of());
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("We should have shazaam twice", count(content, "Shazaam") == 2);
	}
	
	private int count(String doc, String research) {
		int count = 0;
		int pos = 0;
		int newPos;
		while((newPos = doc.indexOf(research, pos)) >= 0) {
			count++;
			pos = newPos + research.length();
		}
		return count;
	}
	
	private String convertToHtml(File file) {
		try (InputStream stream = new FileInputStream(file)) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				HtmlConverter.convert(stream, out);
				out.close();
				return new String(out.toByteArray(), "UTF-8");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
