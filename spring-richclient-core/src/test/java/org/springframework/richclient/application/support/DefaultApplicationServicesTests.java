package org.springframework.richclient.application.support;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.binding.value.ValueChangeDetector;
import org.springframework.binding.value.support.DefaultValueChangeDetector;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.richclient.application.ServiceNotFoundException;
import org.springframework.richclient.image.IconSource;
import org.springframework.richclient.test.SpringRichTestCase;
import org.springframework.rules.RulesSource;

/**
 * Test cases for {@link DefaultApplicationServices}
 * 
 * @author Larry Streepy
 * 
 */
public class DefaultApplicationServicesTests extends SpringRichTestCase {

	@Test
	public void testRegisteredServiceIsReturned() {
		ValueChangeDetector vcd = new DefaultValueChangeDetector();
		getApplicationServices().setValueChangeDetector(vcd);
		assertSame(vcd, getApplicationServices().getService(ValueChangeDetector.class), "Expected same object back");

		MessageSource msrc = new StaticMessageSource();
		getApplicationServices().setMessageSource(msrc);
		assertSame(msrc, getApplicationServices().getService(MessageSource.class), "Expected same object back");
	}

	@Test
	public void testUnknownServiceFails() {
		try {
			getApplicationServices().getService(getClass());
			fail("Unknown service should have caused an exception");
		} catch (ServiceNotFoundException e) {
			; // expected
		}
	}

	@Test
	public void testSetRegistryEntries() {
		ValueChangeDetector vcd = new DefaultValueChangeDetector();
		MessageSource msrc = new StaticMessageSource();

		HashMap entries = new HashMap();
		entries.put("org.springframework.binding.value.ValueChangeDetector", vcd);
		entries.put("org.springframework.context.MessageSource", msrc);

		getApplicationServices().setRegistryEntries(entries);

		assertSame(vcd, getApplicationServices().getService(ValueChangeDetector.class), "Expected same object back");
		assertSame(msrc, getApplicationServices().getService(MessageSource.class), "Expected same object back");
	}

	@Test
	public void testDefaultServicesImplementInterface() {
		Object rulesSource = getApplicationServices().getService(RulesSource.class);
		assertTrue(rulesSource instanceof RulesSource, "Returned service must implement service type");

		Object iconSource = getApplicationServices().getService(IconSource.class);
		assertTrue(iconSource instanceof IconSource, "Returned service must implement service type");
	}
}
