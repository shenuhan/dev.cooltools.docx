package dev.cooltools.docx.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.cooltools.docx.service.FusionServiceFactory;

public class SpeedTest {
	@Test
	public void speedTest() throws Exception {
		String json =
				  "{"
					+ "\"civilite\":\"Madame\","
					+ "\"nom\":\"Tarte\","
					+ "\"prenom\":\"Peche\","
					+ "\"copains\": ["
					+ "{"
						+ "\"civilite\":\"Monsieur\","
						+ "\"nom\":\"Lolo\","
						+ "\"prenom\":\"cool\","
						+ "\"copains\": []"
					+ "},{"
						+ "\"civilite\":\"Monsieur2\","
						+ "\"nom\":\"Lolo2\","
						+ "\"prenom\":\"cool2\","
						+ "\"copains\": []"
					+ "},{"
						+ "\"civilite\":\"Miss3\","
						+ "\"nom\":\"Copain3\","
						+ "\"prenom\":\"Copain3\","
						+ "\"copains\": ["
						+ "{"
							+ "\"civilite\":\"Civ1\","
							+ "\"nom\":\"Simpson1\","
							+ "\"prenom\":\"lisa1\","
							+ "\"copains\": []"
						+ "},{"
							+ "\"civilite\":\"Civ2\","
							+ "\"nom\":\"Simpson2\","
							+ "\"prenom\":\"lisa2\","
							+ "\"copains\": []"
						+ "}]"
					+ "}]"
				+ "}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		byte[] fileBytes;
		try (InputStream stream = SpeedTest.class.getResourceAsStream("/LoopRowImbriqueesTest.docx")) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				IOUtils.copy(stream, out);
				out.flush();
				fileBytes = out.toByteArray();
			}
		}
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		StopWatch afterFirst = new StopWatch();
		for (int i = 0; i < 20; i++) {
			if (i == 1) {
				afterFirst.start();
			}
			try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					FusionServiceFactory.get().merge(stream, out, Collections.singletonMap("personne",node));
				}
			}
		}
		System.out.print("time per fusion in milliseconds: " + stopWatch.getTime(TimeUnit.MILLISECONDS)/20);
		System.out.print("time per fusion after first in milliseconds: " + afterFirst.getTime(TimeUnit.MILLISECONDS)/19);
		
		// @jean the 18/09/2019 was 156 ms each after first
	}
}
