package org.springframework.richclient.settings.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.springframework.richclient.settings.Settings;
import org.springframework.richclient.settings.SettingsAbstractTests;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RootXmlSettingsTests extends SettingsAbstractTests {
	private StringXmlSettingsReaderWriter readerWriter;

	private Document document;

	private Element element;

	@Test
	public void testConstructor() {
		RootXmlSettings settings = new RootXmlSettings(document, readerWriter);
		assertEquals(document, settings.getDocument());
		assertEquals(element, settings.getElement());
		assertEquals("user", settings.getName());
	}

	@Override
	protected void doSetUp() throws Exception {
		readerWriter = new StringXmlSettingsReaderWriter();

		document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		element = document.createElement("settings");
		element.setAttribute("name", "user");
		document.appendChild(element);
	}

	@Override
	protected Settings createSettings() throws Exception {
		StringXmlSettingsReaderWriter readerWriter = new StringXmlSettingsReaderWriter();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element element = doc.createElement("settings");
		element.setAttribute("name", "user");
		doc.appendChild(element);

		return new RootXmlSettings(doc, readerWriter);
	}
}
