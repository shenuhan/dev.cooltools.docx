package dev.cooltools.docx.service;

public class FusionServiceFactory {
	static private FusionService fusionService = new FusionServiceImpl();

	public synchronized static FusionService get() {
		return FusionServiceFactory.fusionService;
	}
}
