package dev.cooltools.docx.util;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

public class RunUtil {

	private static ObjectFactory factory = Context.getWmlObjectFactory();

	private RunUtil() {

	}

	/**
	 * Returns the text string of a run.
	 *
	 * @param run the run whose text to get.
	 * @return String representation of the run.
	 */
	public static String getText(R run) {
		String result = "";
		for (Object content : run.getContent()) {
			if (content instanceof JAXBElement) {
				JAXBElement<?> element = (JAXBElement<?>) content;
				if (element.getValue() instanceof Text) {
					Text textObj = (Text) element.getValue();
					String text = textObj.getValue();
					if (!"preserve".equals(textObj.getSpace())) {
						// trimming text if spaces are not to be preserved (simulates behavior of Word;
						// LibreOffice seems
						// to ignore the "space" property and always preserves spaces)
						text = text.trim();
					}
					result += text;
				} else if (element.getValue() instanceof R.Tab) {
					result += "\t";
				}
			} else if (content instanceof Text) {
				result += ((Text) content).getValue();
			}
		}
		return result;
	}

	/**
	 * Sets the text of the given run to the given value.
	 *
	 * @param run  the run whose text to change.
	 * @param text the text to set.
	 */
	public static void setText(R run, String text) {

		run.getContent().clear();
		Text textObj = factory.createText();
		textObj.setSpace("preserve");
		textObj.setValue(text);
		textObj.setSpace("preserve"); // make the text preserve spaces
		run.getContent().add(textObj);
		if (text != null) {
			String[] textSplit = text.split("\\r\\n");
			if (textSplit.length > 1) {
				run.getContent().clear();
				for (String subtext : textSplit) {
					Text tmpTextObj = factory.createText();
					tmpTextObj.setSpace("preserve");
					tmpTextObj.setValue(subtext);
					tmpTextObj.setSpace("preserve"); // make the text preserve spaces
					run.getContent().add(tmpTextObj);
					run.getContent().add(factory.createBr());
				}
			}
		}
	}

	/**
	 * Creates a new run with the specified text.
	 *
	 * @param text the initial text of the run.
	 * @return the newly created run.
	 */
	public static R create(String text) {
		R run = factory.createR();
		setText(run, text);
		return run;
	}
}
