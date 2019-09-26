package dev.cooltools.docx.processor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.docx.operations.ParagraphOperationsTest;
import dev.cooltools.docx.docx.operations.RowOperationsTest;
import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.service.FusionServiceFactory;
import dev.cooltools.docx.service.Property;
import dev.cooltools.docx.service.Property.VariableType;

public class VariableGetterTest {
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void commentTextOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/CommentTextOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			
			Assert.assertTrue("There should be a test1 property and a test7", 
					properties.size() == 2 
					&& properties.stream().filter(p -> p.getName().equals("test1")).findAny().isPresent()
					&& properties.stream().filter(p -> p.getName().equals("test7")).findAny().isPresent());
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	@Test
	public void inlineTextOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/InlineTextOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));

			Assert.assertTrue("There should be a test1 property", 
					properties.size() == 1 
					&& properties.stream().filter(p -> p.getName().equals("test1")).findAny().isPresent());
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	
	@Test
	public void blockHideShowOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/HideShowOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			Assert.assertTrue("There should be no properties", properties.isEmpty());
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	private boolean checkLeftRightSubproperty(List<Property> properties, String name) {
		Property property = properties.stream().filter(p -> p.getName().equals(name)).findAny().orElse(null);
		Assert.assertNotNull(property);
		return property.getProperties().stream().filter(p -> p.getName().equals("left")).findAny().isPresent() &&
			property.getProperties().stream().filter(p -> p.getName().equals("right")).findAny().isPresent();
	}
	
	private void check(List<Property> properties, String name, VariableType type) {
		Property property = properties.stream().filter(p -> p.getName().equals(name)).findAny().orElse(null);
		Assert.assertNotNull("There should be a property " + name, properties);
		Assert.assertEquals("The property should be of type " + type.name(), type, property.getType());
	}
	
	private void check(List<Property> properties, VariableType type, String... names) {
		String previous = "";
		Property property = null;
		for (String name: names) {
			property = properties.stream().filter(p -> p.getName().equals(name)).findAny().orElse(null);
			Assert.assertNotNull("There should be a property " + previous + name, property);
			properties = property.getProperties();
			previous += name + "."; 
		}
		Assert.assertNotNull("There should be a property" + previous, property);
		Assert.assertEquals("The property type should be the one expected", type, property.getType());
	}
	
	@Test
	public void blockRepeatOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/RepeatOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));

			check(properties, "emptyList", VariableType.List);
			check(properties, "oneElementList", VariableType.List);
			check(properties, "threElementList", VariableType.List);
			check(properties, "oneElementList2", VariableType.List);
			check(properties, "threElementList2", VariableType.List);
			
			checkLeftRightSubproperty(properties, "emptyList");
			checkLeftRightSubproperty(properties, "oneElementList");
			checkLeftRightSubproperty(properties, "threElementList");
			checkLeftRightSubproperty(properties, "oneElementList2");
			checkLeftRightSubproperty(properties, "threElementList2");
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	@Test
	public void embeddedBlockRepeatOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/EmbeddedRepeatOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			check(properties, VariableType.String, "tree", "name");
			check(properties, VariableType.List, "tree", "subTrees");
			check(properties, VariableType.String, "tree", "subTrees", "name");
			check(properties, VariableType.String, "tree", "subTrees", "subTrees", "name");
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	@Test
	public void hideShowOperations() throws Exception {
		try (var in = VariableGetterTest.class.getResourceAsStream("/variable/ParagraphHideShowOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			check(properties, VariableType.String, "test", "evaluate", "unexisting");
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	@Test
	public void repeatOperations() throws Exception {
		try (var in = ParagraphOperationsTest.class.getResourceAsStream("/variable/RepeatOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			check(properties, VariableType.List, "emptyList");
			check(properties, VariableType.String, "emptyList", "right");
			check(properties, VariableType.String, "emptyList", "left");
			check(properties, VariableType.List, "oneElementList");
			check(properties, VariableType.String, "oneElementList", "right");
			check(properties, VariableType.String, "oneElementList", "left");
			check(properties, VariableType.List, "threElementList");
			check(properties, VariableType.String, "threElementList", "right");
			check(properties, VariableType.String, "threElementList", "left");
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	
	@Test
	public void rowLoopOperations() throws Exception {
		try (var in = RowOperationsTest.class.getResourceAsStream("/row/RowOperationsTest.docx")) {
			var properties = FusionServiceFactory.get().findAllVariables(in);
			System.out.println(new ObjectMapper().writeValueAsString(properties));
			
			check(properties, VariableType.List, "emptyList");
			check(properties, VariableType.String, "emptyList", "col1");
			check(properties, VariableType.String, "emptyList", "col2");
			check(properties, VariableType.String, "emptyList", "col3");
			check(properties, VariableType.String, "emptyList", "col4");
			check(properties, VariableType.List, "list");
			check(properties, VariableType.String, "list", "col1");
			check(properties, VariableType.String, "list", "col2");
			check(properties, VariableType.String, "list", "col3");
			check(properties, VariableType.String, "list", "col4");
		} catch (DocxProcessingException e) {
			Assert.assertFalse("there should be no error but there was" + mapper.writeValueAsString(e.getReporter().getErrors()), e.getReporter().hasErrors());
		}
	}
	

}
