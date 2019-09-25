package dev.cooltools.docx.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.html.SdtToListSdtTagHandler;
import org.docx4j.convert.out.html.SdtWriter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * This sample uses XSLT (and Xalan) to produce HTML output. (There is also
 * HtmlExporterNonXSLT for environments where that is not desirable eg Android).
 *
 * If the source docx contained a WMF, that will get converted to inline SVG. In
 * order to see the SVG in your browser, you'll need to rename the file to .xml
 * or serve it with MIME type application/xhtml+xml
 *
 */
public class HtmlConverter {

	static boolean save;
	static boolean nestLists;

	public static void convert(InputStream inputstream, OutputStream out) throws Docx4JException {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputstream);
		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setWmlPackage(wordMLPackage);

		if (nestLists) {
			SdtWriter.registerTagHandler("HTML_ELEMENT", new SdtToListSdtTagHandler());
		} else {
			htmlSettings.getFeatures().remove(ConversionFeatures.PP_HTML_COLLECT_LISTS);
		}
		// If you want XHTML output
		Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
		Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}