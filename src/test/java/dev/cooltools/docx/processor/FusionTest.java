package dev.cooltools.docx.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocGenerator;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.service.FusionServiceFactory;
import dev.cooltools.docx.util.HtmlConverter;

public class FusionTest {
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

	@Test
	public void loopOnMultipleParagraphTest() throws Exception {
		File file = File.createTempFile("LoopOnMultipleParagraphTest", ".docx"); //fichier de fin
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopOnMultipleParagraphTest.docx")) { // get template
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, 
					Collections.singletonMap(
							"personne", 
							new Person("Madame", "Tarte", "Peche",
									new Person("Monsieur", "Lolo", "cool"),
									new Person("Monsieur2", "Lolo2", "cool2"),
									new Person("Miss", "Tartea", "Pion")
							)
						)
					);
			}
		}
		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Miss Pion", "Dont le nom de famille est Tartea"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Tarte Peche", "une fois aussi"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));
	}

	@Test
	public void rowLoopTest() throws Exception {
		File file = File.createTempFile("RowLoopTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/RowLoopTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap( "personne",
						new Person("Madame", "Tarte", "Peche",
								new Person("Monsieur", "Lolo", "cool"),
								new Person("Monsieur2", "Lolo2", "cool2"),
								new Person("Miss", "Tartea", "Pion")
								)));
			}
		}
		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Friens of Tarte", "Tartea", "Lolo"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "Fin de la boucle"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));
	}

	@Test
	public void loopOnEmptyParagrapheTest() throws Exception {
		File file = File.createTempFile("LoopOnEmptyParagraphTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopOnEmptyParagraphTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap(
					"personne2",new Person("Madame", "Tarte", "Peche")
				));
			}
		}
		String content = convertToHtml(file).replaceAll("<.*?>", "");

		for(String checknot : Arrays.asList("Friens of Tarte", "Tartea", "Lolo", "tusseau", "jean", "Haderer", "Dont le nom de famille" ))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));
	}

	@Test
	public void loopImbriqueeParagraphTest() throws Exception {
		File file = File.createTempFile("LoopImbriqueesTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopImbriqueesTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",
						new Person("Madame", "Tarte", "Peche",
								new Person("Monsieur", "Lolo", "cool"),
								new Person("Monsieur2", "Lolo2", "cool2"),
								new Person("Miss3", "Copain3", "Copain3",
										new Person("Miss", "Simpson", "lisa"),
										new Person("Miss", "Copain", "2"))
								)));
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + new ObjectMapper().writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}

		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Par exemple"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi", "Monsieur cool",
				"Monsieur2 cool2", "Miss3 Copain3", "Miss Simpson lisa", "Miss Copain 2"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}
	@Test
	public void loopImbriqueeParagraphTest2() throws Exception {
		File file = File.createTempFile("LoopImbriqueesTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopImbriqueesTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",
						new Person("Madame", "Tarte", "Peche",
								new Person("Monsieur", "Lolo", "cool"),
								new Person("Miss3", "Copain3", "Copain3",
										new Person("Miss", "Simpson", "lisa")
									)
								)));
			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + new ObjectMapper().writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}

		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Par exemple"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi", "Miss3 Copain3", "Miss Simpson lisa"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}


	@Test
	public void loopRowImbriqueeParagraphTest() throws Exception {
		File file = File.createTempFile("LoopRowImbriqueesTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopRowImbriqueesTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",
						new Person("Madame", "Tarte", "Peche",
								new Person("Monsieur", "Lolo", "cool"),
								new Person("Monsieur2", "Lolo2", "cool2"),
								new Person("Miss3", "Copain3", "Copain3",
										new Person("Civ1", "Simpson1", "lisa1"),
										new Person("Civ2", "Simpson2", "lisa2"))
								)));

			}
		}

		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Par exemple"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi", "Monsieur cool",
				"Monsieur2 cool2", "Miss3 Copain3", "Civ1", "Civ2", "Simpson1", "Simpson2", "lisa1", "lisa2"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}

	@Test
	public void testJsonNodeTest() throws Exception {
		String json =
				  "{"
					+ "\"civilite\":\"Madame\","
					+ "\"nom\":\"Tarte\","
					+ "\"prenom\":\"Peche\","
					+ "\"copains\": ["
					+ "{"
						+ "\"civilite\":\"Monsieur\","
						+ "\"nom\":\"Lolo\","
						+ "\"prenom\":\"cool\","
						+ "\"copains\": []"
					+ "},{"
						+ "\"civilite\":\"Monsieur2\","
						+ "\"nom\":\"Lolo2\","
						+ "\"prenom\":\"cool2\","
						+ "\"copains\": []"
					+ "},{"
						+ "\"civilite\":\"Miss3\","
						+ "\"nom\":\"Copain3\","
						+ "\"prenom\":\"Copain3\","
						+ "\"copains\": ["
						+ "{"
							+ "\"civilite\":\"Civ1\","
							+ "\"nom\":\"Simpson1\","
							+ "\"prenom\":\"lisa1\","
							+ "\"copains\": []"
						+ "},{"
							+ "\"civilite\":\"Civ2\","
							+ "\"nom\":\"Simpson2\","
							+ "\"prenom\":\"lisa2\","
							+ "\"copains\": []"
						+ "}]"
					+ "}]"
				+ "}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		File file = File.createTempFile("LoopRowImbriqueesTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/LoopRowImbriqueesTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",node));
			}
		}

		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Par exemple"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi", "Monsieur cool",
				"Monsieur2 cool2", "Miss3 Copain3", "Civ1", "Civ2", "Simpson1", "Simpson2", "lisa1", "lisa2"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}


	@Test
	public void allDisplayTest() throws Exception {
		String value = "{\"text\":\"Show this text\",\"othertext\":\"Show this other text\"}";
		String boolvalue = "{\"false\":false,\"true\":true}";

		ObjectMapper mapper = new ObjectMapper();
		JsonNode valuenode = mapper.readTree(value);
		JsonNode boolvaluenode = mapper.readTree(boolvalue);
		File file = File.createTempFile("AllDisplayTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/AllDisplayTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Map.of(
					"value",valuenode,
					"bool",boolvaluenode
				));

			}
		}
		String content = convertToHtml(file).replaceAll("<.*?>", "");

		for(String checknot : Arrays.asList("texttohide", "texttoreplace","Block true but parent to hide"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Show this text", "Show this other text", "this text should not be hidden","Block to show"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}
	
	@Test 
	public void updateTableOfContentTest()  throws Exception {
		File file = File.createTempFile("AddTableOfContentTest", ".docx");
		
		try (InputStream stream = FusionTest.class.getResourceAsStream("/AddTableOfContentTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.emptyMap());

			}
		} 
		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file);    	
        
		TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        tocGenerator.updateToc( true); // true --> skip page numbering; until docx4j-export-fo is avalaible for java 11
	    wordMLPackage.save(file);
	}
	
	@Test
	public void blockIfLoopRowImbriqueeParagraphTest() throws Exception {
		File file = File.createTempFile("BlockIfLoopRowImbriqueesTest", ".docx");
		try (InputStream stream = FusionTest.class.getResourceAsStream("/BlockIfLoopRowImbriqueesTest.docx")) {
			try (OutputStream out = new FileOutputStream(file)) {
				FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",
						new Person("Madame", "Tarte", "Peche",
								new Person("Monsieur", "Lolo", "cool"),
								new Person("Monsieur2", "Lolo2", "cool2"),
								new Person("Miss3", "Copain3", "Copain3",
										new Person("Civ1", "Simpson1", "lisa1"),
										new Person("Civ2", "Simpson2", "lisa2"))
								)));

			} catch (DocxProcessingException e) {
				Assert.assertFalse("there should be no error but there was" + new ObjectMapper().writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
			}
		}

		String content = convertToHtml(file).replaceAll("<.*?>", "");
		for(String check : Arrays.asList("Par exemple"))
			Assert.assertTrue("File should contain " + check, content.contains(check));

		for(String checknot : Arrays.asList("tusseau", "jean", "Haderer"))
			Assert.assertTrue("File should not contain " + checknot, !content.contains(checknot));

		for(String checkonce : Arrays.asList("Madame Tarte Peche", "une fois aussi", "Monsieur cool","Il a lui aussi ",
				"Monsieur2 cool2", "Miss3 Copain3", "Civ1", "Civ2", "Simpson1", "Simpson2", "lisa1", "lisa2"))
			Assert.assertTrue("File should contain " + checkonce + " once", content.indexOf(checkonce) > 0 && content.indexOf(checkonce) == content.lastIndexOf(checkonce));

	}
}
