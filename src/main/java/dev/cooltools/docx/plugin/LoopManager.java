package dev.cooltools.docx.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.jvnet.jaxb2_commons.ppp.Child;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.error.ErrorType;

public class LoopManager {
	static public enum LoopType {
		Paragraph, Block
	}

	static private class Loop {
		final private LoopType loopType;
		final private Loop parentLoop;
		final private List<Object> objects;
		final private String name;
		int currentObject = 0;

		private Set<Child> cloneRef = new HashSet<>();
		private List<Child> clonedParagraphs = new ArrayList<>();
		private List<Object> toProcessAfterParagraphEnds = new ArrayList<>();

		public Loop(List<Object> objects, String name, Loop parentLoop, LoopType loopType) {
			this.parentLoop = parentLoop;
			this.name = name;
			this.objects = objects;
			this.loopType = loopType;
		}

		public void cloneIfNecessary(Child parentParagraph) {
			if (currentObject < objects.size()) {
				if (parentParagraph != null && cloneRef.add(parentParagraph)) {
					Child clone = (Child) XmlUtils.deepCopy(parentParagraph);
					clone.setParent(parentParagraph.getParent());
					clonedParagraphs.add(clone);
				}
			}
			if (parentLoop != null && currentObject <= 1) {
				parentLoop.cloneIfNecessary(parentParagraph);
			}
		}
	}

	private final Context context;
	private final List<Loop> loops = new ArrayList<Loop>();
	
	private Loop currentLoop;
	
	private Tbl currentTable;
	private R currentRun;

	public LoopManager(Context context) {
		this.context = context;
		
		context.getDocumentCrawler()
			.subscribe(DocxCrawler.ProcessRun, r -> currentRun = r)
			.subscribe(DocxCrawler.StartParagraph, this::startParagraph)
			.subscribe(DocxCrawler.EndParagraph, this::endParagraph)
			.subscribe(DocxCrawler.StartTable, this::startTable)
			.subscribe(DocxCrawler.EndTable, this::endTable);
	}

	public boolean loop(Collection<Object> objects, String name, LoopType loopType) {
		if (StringUtils.isBlank(name)) {
			context.getProblemReporter().reportError(ErrorType.VariableNameNotEmpty);
			return true;
		}
		if (currentLoop != null && name.equals(currentLoop.name)) {
			// here we are processing the nieme occurence of the loop
			currentLoop.cloneIfNecessary(getParentOfRun());
		} else if (this.context.getExpressionEvaluator().existsVariable(name)) {
			this.context.getProblemReporter().reportError(ErrorType.ExistingVariable, name);
		} else {
			currentLoop = new Loop(List.copyOf(objects), name, currentLoop, loopType);

			currentLoop.cloneIfNecessary(getParentOfRun());
			loops.add(currentLoop);

			return getNextObject();
		}
		return true;
	}

	private boolean getNextObject() {
		if (currentLoop.currentObject < currentLoop.objects.size()) {
			this.context.getExpressionEvaluator().addVariable(currentLoop.name, currentLoop.objects.get(currentLoop.currentObject));
			currentLoop.currentObject++;
			return true;
		}
		return false;
	}

	private void endParagraph(P paragraph) {
		if (this.currentLoop != null && this.currentLoop.loopType == LoopType.Paragraph) {
			endloop(currentLoop.name);
		}
		if (this.currentLoop != null && this.currentTable == null && this.currentLoop.toProcessAfterParagraphEnds != null) {
			var toProcess = currentLoop.toProcessAfterParagraphEnds;
			currentLoop.toProcessAfterParagraphEnds = null;
			toProcess.stream().forEach(p ->
				this.context.getDocumentCrawler().addElementToCrawl(paragraph, p)
			);
		}
	}

	private void endTable(Tbl tbl) {
		this.currentTable = null;
		
		if (this.currentLoop != null && this.currentLoop.toProcessAfterParagraphEnds != null) {
			var toProcess = currentLoop.toProcessAfterParagraphEnds;
			currentLoop.toProcessAfterParagraphEnds = null;
			toProcess.stream().forEach(p ->
				this.context.getDocumentCrawler().addElementToCrawl(tbl, p)
			);
		}
	}

	private void startParagraph(P paragraph) {
		// we should clone all Higher element for next loop so paragraph not in table
		// and ...
		if (this.currentLoop != null && this.currentLoop.loopType == LoopType.Block && this.currentTable == null) {
			this.currentLoop.cloneIfNecessary(paragraph);
		}
	}

	private void startTable(Tbl table) {
		this.currentTable = table;
		// ...and Tables
		if (this.currentLoop != null && this.currentLoop.loopType == LoopType.Block) {
			this.currentLoop.cloneIfNecessary(table);
		}
	}

	public boolean endloop(String name) {
		// logger.info("endloop -> " + name);
		if (currentLoop == null) {
			this.context.getProblemReporter().reportError(ErrorType.EndBlockWithNoStart);
			return true;
		}
		if (getNextObject()) {
			List<Child> clonedParagraphs = currentLoop.clonedParagraphs;
			currentLoop.clonedParagraphs = new ArrayList<>();
			context.getPostProcessor().registerAddElements(getParentOfRun(), clonedParagraphs);

			currentLoop.toProcessAfterParagraphEnds = clonedParagraphs.stream().map(o -> (Object) o).collect(Collectors.toList());
		} else {
			Loop removed = loops.remove(loops.size() - 1);
			context.getExpressionEvaluator().removeVariable(removed.name);
			currentLoop = loops.isEmpty() ? null : loops.get(loops.size() - 1);
		}

		return true;
	}

	private Child getParentOfRun() {
		if (currentTable != null)
			return currentTable;

		Child o = currentRun;
		while (o != null) {
			if (o instanceof Child) {
				Object parent = ((Child) o).getParent();
				if (XmlUtils.unwrap(parent) instanceof Body) {
					return o;
				}
				o = parent instanceof Child ? (Child) parent : null;
			} else {
				o = null;
			}
		}
		return o;
	}
}
