package dev.cooltools.docx.error;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Test;

import dev.cooltools.docx.error.DocxProcessingException;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.service.FusionServiceFactory;

public class ErrorTests {
	@Test
	public void errorTest() throws IOException {
		
		try (InputStream stream = ErrorTests.class.getResourceAsStream("/error/ErrorTest.docx")) {
			FusionServiceFactory.get().findAllVariables(stream);
		} catch (DocxProcessingException e) {
			Locale.setDefault(Locale.ENGLISH);
			e.getReporter().getErrors().forEach(er -> System.out.println(er.getLocalizedErrorMessage()));
			e.getReporter().getErrors().stream().filter(er -> er.getEvaluation() != null).forEach(er -> System.out.println(er.getEvaluation().toLocalizedString()));
			Locale.setDefault(Locale.FRENCH);
			e.getReporter().getErrors().forEach(er -> System.out.println(er.getLocalizedErrorMessage()));
			e.getReporter().getErrors().stream().filter(er -> er.getEvaluation() != null).forEach(er -> System.out.println(er.getEvaluation().toLocalizedString()));
		}
	}

	@Test
	public void internationalization() {
		Locale.setDefault(Locale.ENGLISH);
		assertEquals("There was an error while parsing the comment (erroneousComment).", ErrorType.ParseComment.getLocalizedMessage("erroneousComment"));
		Locale.setDefault(Locale.FRENCH);
		assertEquals("Une erreur s'est produite lors de l'exécution du commentaire (erroneousComment).", ErrorType.ParseComment.getLocalizedMessage("erroneousComment"));
	}
}
