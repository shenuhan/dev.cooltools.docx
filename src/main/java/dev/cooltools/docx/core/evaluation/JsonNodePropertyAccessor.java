package dev.cooltools.docx.core.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodePropertyAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] { JsonNode.class };
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof ObjectNode) {
			return ((ObjectNode) target).findValue(name) != null;
		}
		return false;
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof ObjectNode) {
			JsonNode jsonNode = ((ObjectNode) target).findValue(name);
			return new TypedValue(convertJsonNodeLiteralToJavaValue(jsonNode));
		}
		return null;
	}

	static public Object convertJsonNodeLiteralToJavaValue(JsonNode jsonNode) {
		if (jsonNode.isArray()) {
			List<JsonNode> result = new ArrayList<JsonNode>();
			((ArrayNode) jsonNode).iterator().forEachRemaining(jn -> result.add(jn));
			return result;
		}
		if (jsonNode.isBoolean())
			return jsonNode.asBoolean();
		if (jsonNode.isDouble())
			return jsonNode.asDouble();
		if (jsonNode.isLong())
			return jsonNode.asLong();
		if (jsonNode.isTextual())
			return jsonNode.asText();
		return jsonNode;
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}

}
