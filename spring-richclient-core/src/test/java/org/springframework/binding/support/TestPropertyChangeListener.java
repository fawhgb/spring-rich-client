/*
 * Copyright 12/04/2005 (C) Our Community Pty. Ltd. All Rights Reserved.
 *
 * $Id$
 */
package org.springframework.binding.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of PropertyChangeListener that logs all events received.
 * Intended to be used in unit tests.
 * 
 * @author Oliver Hutchison
 */
public class TestPropertyChangeListener implements PropertyChangeListener {

	private String onlyForProperty;

	private List eventsRecevied = new ArrayList();

	public TestPropertyChangeListener(String onlyForProperty) {
		this.onlyForProperty = onlyForProperty;
	}

	public void reset() {
		eventsRecevied.clear();
	}

	public List getEventsRecevied() {
		return eventsRecevied;
	}

	public int eventCount() {
		return eventsRecevied.size();
	}

	public PropertyChangeEvent lastEvent() {
		return (PropertyChangeEvent) eventsRecevied.get(eventCount() - 1);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		assertEquals(onlyForProperty, e.getPropertyName(), "Received PropertyChangeEvent for unexpected property");
		eventsRecevied.add(e);
	}

	public void assertEventCount(int count) {
		assertEquals(count, eventCount(), "Listener has received unexpected number of events");
	}

	public void assertLastEvent(int count, Object oldValue, Object newValue) {
		assertEventCount(count);
		assertEquals(oldValue, lastEvent().getOldValue(), "Listener has received unexpected oldValue");
		assertEquals(newValue, lastEvent().getNewValue(), "Listener has received unexpected newValue");
	}

	public void assertLastEvent(int count, boolean oldValue, boolean newValue) {
		assertLastEvent(count, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
	}
}
