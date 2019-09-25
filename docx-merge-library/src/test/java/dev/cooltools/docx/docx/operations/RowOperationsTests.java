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

public class RowOperationsTests {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void rowLoopOperations() throws Exception {
		var variables = Map.of(
				"list", 
				(Object)List.of(
						Map.of("col1", "val11", "col2", "val12", "col3", "val13", "col4", "val14"),
						Map.of("col1", "val21", "col2", "val22", "col3", "val23", "col4", "val24"),
						Map.of("col1", "val31", "col2", "val32", "col3", "val33", "col4", "val34"),
						Map.of("col1", "val41", "col2", "val42", "col3", "val43", "col4", "val44")
				),
				"emptyList", List.of()
		);
		
		var f = File.createTempFile("RowOperationsTest", ".docx");
		try (var in = RowOperationsTests.class.getResourceAsStream("/row/RowOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, variables);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("We should see line 1 val11 val12 val13 val14", content.contains("val11") && content.contains("val12") && content.contains("val13") && content.contains("val14"));
		Assert.assertTrue("We should see line 2 val21 val22 val23 val24", content.contains("val21") && content.contains("val22") && content.contains("val23") && content.contains("val24"));
		Assert.assertTrue("We should see line 3 val31 val32 val33 val34", content.contains("val31") && content.contains("val32") && content.contains("val33") && content.contains("val34"));
		Assert.assertTrue("We should see line 4 val41 val42 val43 val44", content.contains("val41") && content.contains("val42") && content.contains("val43") && content.contains("val44"));

		Assert.assertTrue("We should keep titles", content.contains("Title1") && content.contains("Title2") && content.contains("Title3") && content.contains("Title4"));

		Assert.assertTrue("r.repeat([]) should not see line1 remove1* ", !content.contains("remove1"));
		Assert.assertTrue("r.repeat([]) should not see line2 remove2* ", !content.contains("remove2"));
		Assert.assertTrue("r.hide We should not see removed line3 remove3* ", !content.contains("remove3"));
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
