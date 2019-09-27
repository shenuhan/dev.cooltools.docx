package dev.cooltools.docx.plugin.operation.row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.NopeEvaluationContext;
import dev.cooltools.docx.error.ErrorType;

public class RowManager {
	static private class RowLoop {
		private final String name;
		private final List<Object> remainingObjects;
		private final List<Tr> addedRows = new ArrayList<Tr>();
		
		public RowLoop(String name, Collection<Object> collectionObjects) {
			this.name = name;
			this.remainingObjects = new ArrayList<Object>(collectionObjects);
		}

		public String getName() {
			return name;
		}
		public List<Tr> getAddedRows() {
			return addedRows;
		}
		public List<Object> getRemainingObjects() {
			return remainingObjects;
		}
	}

	private Context context;
	private NopeEvaluationContext nopeEvaluationContext;
	
	private Tr currentRow;
	private Tbl currentTable;
	
	private RowLoop currentRowLoop;
	private boolean hideRow;

	public RowManager(Context context) {
		this.context = context;
		
		nopeEvaluationContext = new NopeEvaluationContext(new HashMap<String, Object>());
		
		context.getDocumentCrawler().subscribe(DocxCrawler.EndRow, this::endRow);
		context.getDocumentCrawler().subscribe(DocxCrawler.StartRow, r -> this.currentRow = r);
		context.getDocumentCrawler().subscribe(DocxCrawler.EndRow, r -> this.hideRow(false));
		context.getDocumentCrawler().subscribe(DocxCrawler.StartTable, t -> this.currentTable = t);
	}


	public void endRow(Tr tr) {
		if (currentRowLoop != null) {
			if (currentRowLoop.getRemainingObjects().isEmpty()) {
				context.getExpressionEvaluator().removeVariable(currentRowLoop.getName());
				currentRowLoop = null;
			} else {
				context.getExpressionEvaluator().addVariable(currentRowLoop.getName(), currentRowLoop.getRemainingObjects().remove(0));
				context.getDocumentCrawler().addElementToCrawl(tr, currentRowLoop.getAddedRows().remove(0));
			}
		}
	}

	public void hide() {
		this.hideRow(true);
	}
	
	public void hideRow(boolean hide) {
		if (hide) {
			context.getPostProcessor().registerRemoveElement(currentRow);
			context.getPostProcessor().registerPostProcessingOperation(() -> {
				if (currentTable.getContent().size() == 0) {
					context.getPostProcessor().registerRemoveElement(currentTable);
				}
			});
			context.getExpressionEvaluator().registerTemporaryContext(nopeEvaluationContext);
		} else {
			if (hideRow) {
				this.context.getExpressionEvaluator().restoreContext();
			}
		}
		hideRow = hide;
	}

	public boolean loop(Collection<Object> collectionObjects, String name) {
		if (StringUtils.isBlank(name)) {
			context.getProblemReporter().reportError(ErrorType.VariableNameNotEmpty);
			return true;
		}
		if (currentRowLoop != null && !name.equals(currentRowLoop.getName())) {
			context.getProblemReporter().reportError(ErrorType.MultipleRowLoop);
			return true;
		}

		if (currentRowLoop == null) {
			if (collectionObjects.isEmpty()) {
				hide();
			} else {
				if (this.context.getExpressionEvaluator().existsVariable(name)) {
					this.context.getProblemReporter().reportError(ErrorType.ExistingVariable, name);
					return true;
				}

				currentRowLoop = new RowLoop(name, collectionObjects);
				context.getExpressionEvaluator().addVariable(currentRowLoop.getName(), currentRowLoop.getRemainingObjects().remove(0));

				Tr rowToDuplicate;
				Tr previousRow = rowToDuplicate = currentRow;

				for (int i = 0; i < currentRowLoop.getRemainingObjects().size(); i++) {
					Tr o = XmlUtils.deepCopy(rowToDuplicate);
					context.getPostProcessor().registerAddElement(previousRow, o);
					currentRowLoop.getAddedRows().add(o);
					previousRow = o;
				}
			}
		}

		return true;
	}

}
