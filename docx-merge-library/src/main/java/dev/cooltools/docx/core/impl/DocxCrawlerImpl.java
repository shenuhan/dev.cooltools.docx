package dev.cooltools.docx.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import dev.cooltools.docx.core.DocxCrawler;

public class DocxCrawlerImpl implements DocxCrawler {
	private final WordprocessingMLPackage document;
	private final Map<Event<?>, List<Consumer<?>>> subscriptions = new HashMap<>();
	private final Map<Object, List<Object>> addedToCrawl = new HashMap<>();

	public DocxCrawlerImpl(WordprocessingMLPackage document) {
		if (document == null) throw new RuntimeException("There was a problem during the init, check the problem reporter");
		
		this.document = document;
	}

	public void crawlDocument() {
		publish(StartDocument, document);
		RelationshipsPart relationshipsPart = document.getMainDocumentPart().getRelationshipsPart();

		// walk through elements in headers
		List<Relationship> headerRelationships = getRelationshipsOfType(document, Namespaces.HEADER);
		for (Relationship header : headerRelationships) {
			HeaderPart headerPart = (HeaderPart) relationshipsPart.getPart(header.getId());
			processContent(headerPart.getContent());
		}

		// walk through elements in main document part
		processContent(document.getMainDocumentPart().getContent());

		// walk through elements in headers
		List<Relationship> footerRelationships = getRelationshipsOfType(document, Namespaces.FOOTER);
		for (Relationship footer : footerRelationships) {
			FooterPart footerPart = (FooterPart) relationshipsPart.getPart(footer.getId());
			processContent(footerPart.getContent());
		}
		publish(EndDocument, document);
	}

	private List<Relationship> getRelationshipsOfType(WordprocessingMLPackage document, String type) {
		List<Relationship> allRelationhips = document.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship();
		List<Relationship> headerRelationships = new ArrayList<>();
		for (Relationship r : allRelationhips) {
			if (r.getType().equals(type)) {
				headerRelationships.add(r);
			}
		}
		return headerRelationships;
	}

	public void processContent(List<Object> contentElements) {
		for (Object contentElement : contentElements) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof P) {
				P p = (P) unwrappedObject;
				processParagraph(p);
			} else if (unwrappedObject instanceof Tbl) {
				Tbl table = (Tbl) unwrappedObject;
				processTable(table);
			}
		}
	}

	private void processParagraph(P p) {
		publish(StartParagraph, p);
		for (Object contentElement : p.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof R) {
				R run = (R) unwrappedObject;
				processRun(run);
			} else if (unwrappedObject instanceof CTSimpleField) {
				CTSimpleField run = (CTSimpleField) unwrappedObject;
				processCTSimpleField(run);
			}
		}
		publish(EndParagraph, p);
		checkCrawlAfter(p);
	}


	private void processCTSimpleField(CTSimpleField ctSimpleField) {
		publish(ProcessCTSimpleField, ctSimpleField);
		checkCrawlAfter(ctSimpleField);
	}

	private void processRun(R run) {
		publish(ProcessRun, run);
		checkCrawlAfter(run);
	}

	private void processTable(Tbl tbl) {
		publish(StartTable, tbl);
		for (Object contentElement : tbl.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof Tr) {
				processRow((Tr) unwrappedObject);
			}
		}
		publish(EndTable, tbl);
		checkCrawlAfter(tbl);
	}

	private void processRow(Tr tr) {
		publish(StartRow, tr);
		for (Object contentElement : tr.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof Tc) {
				processCell((Tc) unwrappedObject);
			}
		}
		publish(EndRow, tr);
		checkCrawlAfter(tr);
	}

	private void processCell(Tc tc) {
		publish(StartCell, tc);
		for (Object contentElement : tc.getContent()) {
			Object unwrappedObject = XmlUtils.unwrap(contentElement);
			if (unwrappedObject instanceof P) {
				processParagraph((P) unwrappedObject);
			}
		}
		publish(EndCell, tc);
		checkCrawlAfter(tc);
	}

	public <Element> DocxCrawler subscribe(Event<Element> event, Consumer<Element> consumer) {
		var pub = subscriptions.get(event); 
		if (pub == null) {
			subscriptions.put(event, pub = new LinkedList<Consumer<?>>());
		}
		pub.add(consumer);
		return this;
	}
	
	private <Element> void publish(Event<Element> event, Element element) {
		var list = subscriptions.get(event);
		if (list == null) {
			return;
		}
		for (var consumer : list) {
			@SuppressWarnings("unchecked")
			var c = (Consumer<Object>) consumer;
			c.accept(element);
		}
	}

	@Override
	public <AfterElementType, AddedElementType> DocxCrawler addElementToCrawl(AfterElementType addAfterMe, AddedElementType toAdd) {
		var alreadyAdded = addedToCrawl.get(addAfterMe);
		if (alreadyAdded == null) {
			addedToCrawl.put(addAfterMe, alreadyAdded = new LinkedList<Object>());
		}
		alreadyAdded.add(toAdd);
		return this;
	}
	

	private void checkCrawlAfter(Object afterMe) {
		var added = addedToCrawl.remove(afterMe);
		if (added == null) {
			return;
		}
		for (var o : added) {
			if (o instanceof P) {
				processParagraph((P) o);
			} else if (o instanceof Tbl) {
				processTable((Tbl) o);
			} else if (o instanceof Tr) {
				processRow((Tr) o);
			} else if (o instanceof Tc) {
				processCell((Tc) o);
			} else if (o instanceof R) {
				processRun((R) o);
			}
		}
	}
}
