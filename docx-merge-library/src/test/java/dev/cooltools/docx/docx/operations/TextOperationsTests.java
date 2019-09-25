package dev.cooltools.docx.docx.operations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.service.FusionServiceFactory;
import dev.cooltools.docx.util.HtmlConverter;

public class TextOperationsTests {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void commentTextOperations() throws Exception {
		var variables = Map.ofEntries(
				Map.entry("test1", (Object) "result1"),
				Map.entry("test7", (Object) "result7")
		);
		
		var f = File.createTempFile("CommentTextOperationsTest", ".docx");
		try (var in = TextOperationsTests.class.getResourceAsStream("/text/CommentTextOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, variables);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		Assert.assertTrue("t.value(#test1) should replace test1 whith result1", content.contains("result1") && !content.contains("test1"));
		Assert.assertTrue("t.show(true) should keep result2", content.contains("result2"));
		Assert.assertTrue("t.show(false) should hide result3", !content.contains("result3"));
		Assert.assertTrue("t.hide(false) should keep result4", content.contains("result4"));
		Assert.assertTrue("t.hide(true) should hide result5", !content.contains("result5"));
		Assert.assertTrue("t.hide() should hide result6", !content.contains("result6"));
		Assert.assertTrue("#test7 should replace test7 with result7", content.contains("result7") && !content.contains("test7"));
	}
	
	@Test
	public void inlineTextOperations() throws Exception {
		var variables = Map.ofEntries(
			Map.entry("test1", (Object) "result1")
		);
		
		var f = File.createTempFile("InlineTextOperationsTest", ".docx");
		try (var in = TextOperationsTests.class.getResourceAsStream("/text/InlineTextOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, variables);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		Assert.assertTrue("t.value(#test1) should replace test1 whith result1", content.contains("result1") && !content.contains("test1"));
	}
	
	@Test
	public void docxPropertyTextOperations() throws Exception {
		var variables = Map.ofEntries(
			Map.entry("replaceme", (Object) "result1")
		);
		
		var f = File.createTempFile("PropertyTextOperationsTest", ".docx");
		try (var in = TextOperationsTests.class.getResourceAsStream("/text/PropertyTextOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, variables);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		Assert.assertTrue("docx property #replaceme should replace replaced field whith result1", content.contains("result1") && !content.contains("replaced field"));
	}
	
	@Test
	public void joinTextOperations() throws Exception {
		var variables = Map.ofEntries(
			Map.entry("stringList", (Object) List.of("e", "fg", "h"))
		);
		
		var f = File.createTempFile("JoinTextOperationsTest", ".docx");
		try (var in = TextOperationsTests.class.getResourceAsStream("/text/JoinTextOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, variables);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		Assert.assertTrue("Join should be replaced whith abcd", content.contains("abcd"));
		Assert.assertTrue("Join List should be replaced whith efgh", content.contains("efgh"));
		Assert.assertTrue("Join List with separator should be replaced whith e - fg - h", content.contains("e - fg - h"));
		Assert.assertTrue("Join List with separator and last should be replaced whith e, fg and h", content.contains("e - fg - h"));
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
