package dev.cooltools.docx.plugin.processor;

import java.util.LinkedList;
import java.util.List;

import org.docx4j.wml.R;

public class MultiRunManager {
	private List<R> runs = new LinkedList<R>();
	
	public void clearRuns() {
		this.runs.clear();
	}
	
	public void addRun(R run) {
		this.runs.add(run);
	}
	
	public List<R> getRuns() {
		return runs;
	}
}
