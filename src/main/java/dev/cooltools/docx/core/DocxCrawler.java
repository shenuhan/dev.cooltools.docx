package dev.cooltools.docx.core;

import java.util.function.Consumer;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

public interface DocxCrawler {
	public static class Event<T> {
	}

	public static Event<WordprocessingMLPackage> StartDocument = new Event<>();
	public static Event<WordprocessingMLPackage> EndDocument = new Event<>();
	
	public static Event<P> StartParagraph = new Event<>();
	public static Event<P> EndParagraph = new Event<>();
	
	public static Event<Tbl> StartTable = new Event<>();
	public static Event<Tbl> EndTable = new Event<>();
	
	public static Event<Tr> StartRow = new Event<>();
	public static Event<Tr> EndRow = new Event<>();
	
	public static Event<Tc> StartCell = new Event<>();
	public static Event<Tc> EndCell = new Event<>();
	
	public static Event<R> ProcessRun = new Event<>();
	public static Event<CTSimpleField> ProcessCTSimpleField = new Event<>();
	
	<Element> DocxCrawler subscribe(Event<Element> event, Consumer<Element> consumer);
	<AfterElementType, AddedElementType> DocxCrawler addElementToCrawl(AfterElementType addAfterMe, AddedElementType toAdd);
}