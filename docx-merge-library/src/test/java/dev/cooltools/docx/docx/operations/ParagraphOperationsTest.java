package dev.cooltools.docx.docx.operations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.service.FusionServiceFactory;
import dev.cooltools.docx.util.HtmlConverter;

public class ParagraphOperationsTest {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void hideShowOperations() throws Exception {
		var f = File.createTempFile("HideShowOperationsTest", ".docx");

		try (var in = ParagraphOperationsTest.class.getResourceAsStream("/paragraph/ParagraphHideShowOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, Collections.emptyMap());
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("p.hide should not affect other paragraph test1", content.contains("test1"));
		Assert.assertTrue("p.hide() should remove the paragraph test2", !content.contains("test2"));
		Assert.assertTrue("p.hide should not affect other paragraph test3", content.contains("test3"));
		Assert.assertTrue("p.hide(true) should remove the paragraph test4 completely", !content.contains("test4"));
		Assert.assertTrue("p.hide(false) should not remove the paragraph test5", content.contains("test5"));
		Assert.assertTrue("p.show(false) should remove the paragraph test6", !content.contains("test6"));
		Assert.assertTrue("p.show(true) should not remove the paragraph test7", content.contains("test7"));
		Assert.assertTrue("p.show(false or true) should not affect other paragraph test8", content.contains("test8"));
	}
	
	@Test
	public void repeatOperations() throws Exception {
		var f = File.createTempFile("RepeatOperationsTest", ".docx");
		Map<String, Object> data = Map.of(
				"emptyList", Collections.emptyList(), 
				"oneElementList", List.of(new ImmutablePair<String, String>("lresultat3", "rresultat3")), 
				"threElementList", List.of(
						new ImmutablePair<String, String>("lresultat51", "rresultat51"), 
						new ImmutablePair<String, String>("lresultat52", "rresultat52"),
						new ImmutablePair<String, String>("lresultat53", "rresultat53")
					) 
			);

		try (var in = ParagraphOperationsTest.class.getResourceAsStream("/paragraph/ParagraphRepeatOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, data);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("p.repeat should not affect other paragraph test1", content.contains("test1"));
		Assert.assertTrue("p.repeat(emptyList) should be removed", !content.contains("test2"));
		Assert.assertTrue("p.repeat(oneElement) should repeat once", count(content, "test3") == 1);
		Assert.assertTrue("p.repeat should not affect other paragraph test4", content.contains("test4"));
		Assert.assertTrue("p.repeat(3) should repeat 3 times", count(content, "test5") == 3);
		Assert.assertTrue("p.repeat(3) should replace variables", 
				content.contains("lresultat51") && content.contains("rresultat51") &&
				content.contains("lresultat52") && content.contains("rresultat52") &&
				content.contains("lresultat53") && content.contains("rresultat53")
				);
		Assert.assertTrue("p.repeat should not affect other paragraph test6", content.contains("test6"));
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
