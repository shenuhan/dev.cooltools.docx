package dev.cooltools.docx.plugin.operation.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.docx4j.wml.P;
import org.springframework.expression.EvaluationContext;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.core.DocxCrawler;
import dev.cooltools.docx.core.evaluation.NopeEvaluationContext;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.plugin.InjectPluginBean;
import dev.cooltools.docx.plugin.LoopManager;
import dev.cooltools.docx.plugin.LoopManager.LoopType;
import dev.cooltools.docx.plugin.operation.Initialize;
import dev.cooltools.docx.plugin.operation.SimpleOperations;

public class BlockManager extends SimpleOperations implements Initialize {
	private EvaluationContext blockContext;

	private Context context;

	@InjectPluginBean
	private LoopManager loopManager;

	@InjectPluginBean
	private BlockOperations blockOperations;

	static private class Block {
		private boolean display;
		private String loopName;

		Block(boolean display) {
			this.display = display;
		}

		Block(String loopName) {
			this.display = true;
			this.loopName = loopName;
		}
	}

	public Block currentBlock() {
		return blocks.get(0);
	}

	public BlockManager(Context context) {
		this.context = context;
		
		context.getDocumentCrawler()
		  .subscribe(DocxCrawler.StartParagraph, p -> currentParagraph = p)
		  .subscribe(DocxCrawler.StartParagraph, this::startParagraph)
		  .subscribe(DocxCrawler.EndParagraph, p -> currentParagraph = null);
	}
	
	public void initialize() {
		Map<String, Object> operations = new HashMap<>();
		for (String prefix : blockOperations.getPrefixes()) {
			operations.put(prefix, blockOperations);
		}
		this.blockContext = new NopeEvaluationContext(operations);
	}
	
	P currentParagraph;

	List<Block> blocks = new LinkedList<>();

	public void startBlock(boolean display) {
		blocks.add(0, new Block(display));
		if (!currentBlock().display) {
			context.getPostProcessor().registerRemoveElement(currentParagraph);
			// starting now the block won't be displayed so we only watch the block
			// operation until this one is ended
			context.getExpressionEvaluator().registerTemporaryContext(blockContext);
		}
	}

	public void startParagraph(P paragraph) {
		for (Block b : blocks) {
			if (!b.display) {
				this.context.getPostProcessor().registerRemoveElement(currentParagraph);
			}
		}
	}

	public void endBlock() {
		if (blocks.isEmpty()) {
			context.getProblemReporter().reportError(ErrorType.EndBlockWithNoStart);
		} else {
			Block b = blocks.remove(0);
			if (!b.display) {
				if (!blocks.stream().filter(block -> !block.display).findAny().isPresent()) {
					// we had a block that was not displayed and all the remaining are to be
					// displayed so we reactivate the evaluation
					context.getExpressionEvaluator().restoreContext();
				}
			} else if (b.loopName != null) {
				loopManager.endloop(b.loopName);
			}
		}
	}

	public void startLoop(Collection<Object> objects, String variableName) {
		if (objects == null || objects.isEmpty()) {
			startBlock(false);
		} else {
			loopManager.loop(objects, variableName, LoopType.Block);
			this.blocks.add(0, new Block(variableName));
		}
	}
}
