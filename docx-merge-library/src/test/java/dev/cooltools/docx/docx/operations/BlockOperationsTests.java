package dev.cooltools.docx.docx.operations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
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

public class BlockOperationsTests {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void blockHideShowOperations() throws Exception {
		var f = File.createTempFile("BlockHideShowOperationsTest", ".docx");

		try (var in = BlockOperationsTests.class.getResourceAsStream("/block/HideShowOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, Collections.emptyMap());
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("b operation should not affect other paragraph Title", content.contains("Title"));
		Assert.assertTrue("b.show(true) should not remove the paragraph test1", content.contains("test1"));
		Assert.assertTrue("b.hide(false) should not remove the paragraph test2", count(content, "test2") == 3);
		Assert.assertTrue("b.hide() should remove test3", !content.contains("test3"));
		Assert.assertTrue("b.hide(true) should remove test4 completely", !content.contains("test4"));
		Assert.assertTrue("b operation should not affect other paragraph test5", content.contains("test5"));

		// Block 6 show hide show
		Assert.assertTrue("we should still get test60 and test64", content.contains("test60") && content.contains("test64"));
		Assert.assertTrue("we should not see test61, test62, test63 ", !content.contains("test61") && !content.contains("test62") && !content.contains("test63"));

		// Block 7 show show hide
		Assert.assertTrue("we should still get test70, test71, test73, test74", content.contains("test70") && content.contains("test71") && content.contains("test73") && content.contains("test74"));
		Assert.assertTrue("we should not see test72", !content.contains("test72"));
	}
	
	@Test
	public void blockRepeatOperations() throws Exception {
		var f = File.createTempFile("BlockRepeatOperationsTest", ".docx");
		Map<String, Object> data = Map.of(
				"emptyList", Collections.emptyList(), 
				"oneElementList", List.of(new ImmutablePair<String, String>("lresultat13", "rresultat13")), 
				"threElementList", List.of(
						new ImmutablePair<String, String>("lresultat151", "rresultat151"), 
						new ImmutablePair<String, String>("lresultat152", "rresultat152"),
						new ImmutablePair<String, String>("lresultat153", "rresultat153")
					),
				"oneElementList2", List.of(new ImmutablePair<String, String>("lresultat23", "rresultat23")), 
				"threElementList2", List.of(
						new ImmutablePair<String, String>("lresultat251", "rresultat251"), 
						new ImmutablePair<String, String>("lresultat252", "rresultat252"),
						new ImmutablePair<String, String>("lresultat253", "rresultat253")
					) 

			);

		try (var in = ParagraphOperationsTests.class.getResourceAsStream("/block/RepeatOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, data);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("b.repeat should not affect other paragraph test11", content.contains("test11"));
		Assert.assertTrue("b.repeat(emptyList) should be removed", !content.contains("test12"));
		Assert.assertTrue("b.repeat(oneElement) should repeat once", count(content, "test13") == 1);
		Assert.assertTrue("b.repeat should not affect other paragraph test14", content.contains("test14"));
		Assert.assertTrue("b.repeat(3) should repeat 3 times", count(content, "test15") == 3);
		Assert.assertTrue("b.repeat(3) should replace variables", 
				content.contains("lresultat151") && content.contains("rresultat151") &&
				content.contains("lresultat152") && content.contains("rresultat152") &&
				content.contains("lresultat153") && content.contains("rresultat153")
				);
		Assert.assertTrue("p.repeat should not affect other paragraph test16", content.contains("test16"));
		
		// multiparagraph tests
		Assert.assertTrue("b.repeat should not affect other paragraph test21", content.contains("test21"));
		Assert.assertTrue("b.repeat(emptyList) should be removed", !content.contains("test22"));
		Assert.assertTrue("b.repeat(oneElement) should repeat once", count(content, "test23") == 1);
		Assert.assertTrue("b.repeat should not affect other paragraph test24", content.contains("test24"));
		Assert.assertTrue("b.repeat(3) should repeat 3 times test25", count(content, "test25") == 3);
		Assert.assertTrue("b.repeat(3) should replace variables test25", 
				content.contains("lresultat251") && content.contains("rresultat251") &&
				content.contains("lresultat252") && content.contains("rresultat252") &&
				content.contains("lresultat253") && content.contains("rresultat253")
				);
		Assert.assertTrue("p.repeat should not affect other paragraph test26", content.contains("test26"));
	}
	
	public class ObjectTree {
		private final String name;
		private final List<ObjectTree> subTrees;
		
		private ObjectTree(String name, List<ObjectTree> subtrees) {
			this.name = name;
			this.subTrees = subtrees;
		}

		public String getName() {
			return name;
		}

		public List<ObjectTree> getSubTrees() {
			return subTrees;
		}
	}
	
	ObjectTree c(String name, ObjectTree... subtrees) {
		return new ObjectTree(name, Arrays.asList(subtrees));
	}
	
	@Test
	public void embeddedBlockRepeatOperations() throws Exception {
		var f = File.createTempFile("EmbeddedRepeatOperationsTest", ".docx");
		Map<String, Object> data = Map.of("tree",
			c(
				"master", 
				  c("sublist1"),
				  c("sublist2", 
						  c("subsublist21")
				  ),
				  c("sublist3", 
						  c("subsublist31"), 
						  c("subsublist32"), 
						  c("subsublist33")
				  )
			)
		);
				

		try (var in = ParagraphOperationsTests.class.getResourceAsStream("/block/EmbeddedRepeatOperationsTest.docx")) {
			try (var out = new FileOutputStream(f)) {
				FusionServiceFactory.get().merge(in, out, data);
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}
		String content = convertToHtml(f).replaceAll("<.*?>", "");
		
		Assert.assertTrue("b.repeat should show variable sublist1", content.contains("sublist1"));
		Assert.assertTrue("b.repeat should show variable sublist2", content.contains("sublist2"));
		Assert.assertTrue("b.repeat should show variable sublist3", content.contains("sublist3"));
		
		Assert.assertTrue("b.repeat should show variable subsublist21", content.contains("subsublist21"));
		Assert.assertTrue("b.repeat should show variable subsublist31", content.contains("subsublist31"));
		Assert.assertTrue("b.repeat should show variable subsublist32", content.contains("subsublist32"));
		
		
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
