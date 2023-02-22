package org.springframework.richclient.application.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LifecycleApplicationEventTests {

	private class Base {
	}

	private class Child extends Base {
	}

	/**
	 * Simple test to check {@link LifecycleApplicationEvent#objectIs(Class)}.
	 */
	@Test
	public void testEventObjectType() {
		Child child = new Child();
		LifecycleApplicationEvent event = new LifecycleApplicationEvent(LifecycleApplicationEvent.CREATED, child);
		assertTrue(event.objectIs(Base.class), "Child extends Base so objectIs() should return true.");
	}

}
