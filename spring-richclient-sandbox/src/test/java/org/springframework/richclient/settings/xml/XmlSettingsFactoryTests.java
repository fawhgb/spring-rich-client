package org.springframework.richclient.settings.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.richclient.settings.Settings;
import org.springframework.richclient.settings.SettingsException;
import org.w3c.dom.Document;

public class XmlSettingsFactoryTests {

	@Test
	public void testGetAndSetLocation() {
		XmlSettingsFactory settingsFactory = new XmlSettingsFactory();

		assertEquals("settings", settingsFactory.getLocation(), "default settings location is \"settings\"");

		settingsFactory.setLocation("other-settings");
		assertEquals("other-settings", settingsFactory.getLocation());

		settingsFactory.setLocation(null);
		assertEquals("settings", settingsFactory.getLocation(), "location not reset to default");
	}

	@Test
	public void testGetAndSetReaderWriter() {
		XmlSettingsFactory settingsFactory = new XmlSettingsFactory();
		settingsFactory.setLocation("other-settings");

		XmlSettingsReaderWriter readerWriter = settingsFactory.getReaderWriter();
		assertTrue(readerWriter instanceof FileSystemXmlSettingsReaderWriter,
				"default must be FileSystemXmlSettingsReaderWriter");
		// test location
		FileSystemXmlSettingsReaderWriter fileSystemXmlSettingsReaderWriter = (FileSystemXmlSettingsReaderWriter) readerWriter;
		assertEquals("other-settings", fileSystemXmlSettingsReaderWriter.getLocation());

		StringXmlSettingsReaderWriter newReaderWriter = new StringXmlSettingsReaderWriter(null);
		settingsFactory.setReaderWriter(newReaderWriter);
		assertEquals(newReaderWriter, settingsFactory.getReaderWriter());

		settingsFactory.setReaderWriter(null);
		assertTrue(settingsFactory.getReaderWriter() instanceof FileSystemXmlSettingsReaderWriter,
				"not reset to default");
	}

	@Test
	public void testCreate() throws SettingsException {
		XmlSettingsFactory settingsFactory = new XmlSettingsFactory();
		settingsFactory.setReaderWriter(new StringXmlSettingsReaderWriter(null));

		Settings settings = settingsFactory.createSettings("user");
		assertNotNull(settings);
		assertTrue(settings instanceof RootXmlSettings);
		assertEquals("user", settings.getName());

		RootXmlSettings rootXmlSettings = (RootXmlSettings) settings;
		Document document = rootXmlSettings.getDocument();
		assertNotNull(document);
	}
}
