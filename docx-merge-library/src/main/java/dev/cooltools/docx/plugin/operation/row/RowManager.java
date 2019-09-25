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
	private class RowLoop {
		public String name;
		public List<Object> remainingObjects;
		public List<Tr> addedRows = new ArrayList<Tr>();
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
			if (currentRowLoop.remainingObjects.isEmpty()) {
				context.getExpressionEvaluator().removeVariable(currentRowLoop.name);
				currentRowLoop = null;
			} else {
				context.getExpressionEvaluator().addVariable(currentRowLoop.name, currentRowLoop.remainingObjects.remove(0));
				context.getDocumentCrawler().addElementToCrawl(tr, currentRowLoop.addedRows.remove(0));
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
		if (currentRowLoop != null && !name.equals(currentRowLoop.name)) {
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

				currentRowLoop = new RowLoop();
				currentRowLoop.name = name;
				currentRowLoop.remainingObjects = new ArrayList<>(collectionObjects);
				context.getExpressionEvaluator().addVariable(currentRowLoop.name, currentRowLoop.remainingObjects.remove(0));

				Tr rowToDuplicate;
				Tr previousRow = rowToDuplicate = currentRow;

				for (int i = 0; i < currentRowLoop.remainingObjects.size(); i++) {
					Tr o = XmlUtils.deepCopy(rowToDuplicate);
					context.getPostProcessor().registerAddElement(previousRow, o);
					currentRowLoop.addedRows.add(o);
					previousRow = o;
				}
			}
		}

		return true;
	}

}
