package dev.cooltools.docx.plugin.processor.property;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.custom.Properties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.util.RunUtil;

public class PropertyManager {
	private static final Logger log = LoggerFactory.getLogger(PropertyManager.class);

	public static class PropertyWrapper {
		private final Object begin;
		private final Object end;
		private final String property;

		public PropertyWrapper(Object begin, Object end, String property) {
			this.begin = begin;
			this.end = end;
			this.property = property;
		}

		public Object getBegin() {
			return begin;
		}

		public Object getEnd() {
			return end;
		}

		public String getProperty() {
			return property;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PropertyWrapper)) {
				return false;
			}
			return begin != null && begin.equals(((PropertyWrapper) obj).begin);
		}
	}

	final private Map<String, Integer> properties = new HashMap<>();
	final private Pattern propertyPattern = Pattern.compile("^\\s*DOCPROPERTY\\s+\"?(#[^\\s].*?)\"?\\s*$");

	public PropertyManager(Context context) {
		DocPropsCustomPart prop = context.getDocument().getDocPropsCustomPart();
		try {
			if (prop == null || prop.getContents() == null) {
				return;
			}
			for (Properties.Property property : prop.getContents().getProperty()) {
				properties.put(StringUtils.trim(property.getName()), property.getPid());
				if (log.isDebugEnabled()) {
					log.debug("Document property " + property.getFmtid() + "--" + property.getName());
				}
			}
		} catch (Docx4JException e) {
			log.error("Could not access document properties", e);
			context.getProblemReporter().reportError(ErrorType.DocumentInitializing, e.getLocalizedMessage());
			properties.clear();		
		}
	}

	public PropertyWrapper getPropertyWrapper(R run) {
		if (properties.isEmpty()) {
			return null;
		}
		// first look if the current run is a FldChar of type BEGIN (this could be a
		// property)
		if (run.getContent().isEmpty()) {
			return null;
		}
		if (!(XmlUtils.unwrap(run.getContent().get(0)) instanceof FldChar)) {
			return null;
		}
		FldChar fldChar = (FldChar) XmlUtils.unwrap(run.getContent().get(0));
		if (fldChar.getFldCharType() != STFldCharType.BEGIN) {
			return null;
		}

		R begin = null;
		String propertyName = null;
		String property = "";
		for (Object current : ((ContentAccessor) run.getParent()).getContent()) {
			// iterate on the parent to find the begining the end and the property name
			if (current == run) {
				// this is the BEGIN
				begin = (R) current;
			} else if (begin != null && propertyName == null) {
				// we find it the first time or we give up
				if (current instanceof R) {
					R r = (R) current;
					property += RunUtil.getText(r);
					if (!r.getContent().isEmpty() && (XmlUtils.unwrap(r.getContent().get(0)) instanceof FldChar)) {
						FldChar separate = (FldChar) XmlUtils.unwrap(r.getContent().get(0));
						if (separate.getFldCharType() == STFldCharType.SEPARATE) {
							Matcher m = propertyPattern.matcher(property);
							if (!m.matches()) {
								return null;
							}
							propertyName = m.group(1);
							if (!this.properties.containsKey(propertyName)) {
								return null;
							}
						} else {
							return null;
						}
					}
				}
			} else if (begin != null && propertyName != null && current instanceof R) {
				// waiting for the end
				R fld = (R) current;
				if (!fld.getContent().isEmpty() && (XmlUtils.unwrap(fld.getContent().get(0)) instanceof FldChar)) {
					FldChar end = (FldChar) XmlUtils.unwrap(fld.getContent().get(0));
					if (end.getFldCharType() == STFldCharType.END) {
						return new PropertyWrapper(begin, (R) current, propertyName);
					}
				}
			}
		}

		return null;
	}

	public PropertyWrapper getPropertyWrapper(CTSimpleField ctSimpleField) {
		Matcher m = propertyPattern.matcher(ctSimpleField.getInstr());
		if (!m.matches()) {
			return null;
		}
		String propertyName = m.group(1);
		if (!this.properties.containsKey(propertyName)) {
			return null;
		}
		return new PropertyWrapper(ctSimpleField, ctSimpleField, propertyName);
	}
}
