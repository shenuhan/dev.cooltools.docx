package dev.cooltools.docx.plugin.operation;

public class SimpleOperations implements Operations {
	String[] prefixes;
	
	@Override
	public String[] getPrefixes() {
		return prefixes;
	}

	@Override
	public void setPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
	}
}
