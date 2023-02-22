/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

/**
 * Provides a suite of unit tests for the {@link EventListenerListHelper} class.
 * 
 * @author Kevin Stembridge
 * @since 0.3.0
 * 
 */
public class EventListenerListHelperTests {

	/**
	 * Test method for
	 * {@link EventListenerListHelper#EventListenerListHelper(java.lang.Class)}.
	 * Confirms that this constructor throws an IllegalArgumentException if passed a
	 * null argument.
	 */
	@Test
	public void testEventListenerListHelper() {

		try {
			new EventListenerListHelper(null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Test method for {@link EventListenerListHelper#hasListeners()}.
	 */
	@Test
	public void testHasListeners() {

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);
		assertFalse(listHelper.hasListeners(), "Assert list helper has no listeners");
		listHelper.add(new Object());
		assertTrue(listHelper.hasListeners(), "Assert list helper has listeners");

	}

	/**
	 * Test method for {@link EventListenerListHelper#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);
		assertTrue(listHelper.isEmpty(), "Assert list helper is empty");
		listHelper.add(new Object());
		assertFalse(listHelper.isEmpty(), "Assert list helper is not empty");

	}

	/**
	 * Test method for {@link EventListenerListHelper#getListenerCount()}.
	 */
	@Test
	public void testGetListenerCount() {

		Object listener1 = new Object();
		Object listener2 = new Object();
		Object listener3 = new Object();

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);

		assertEquals(0, listHelper.getListenerCount());
		listHelper.add(listener1);
		assertEquals(1, listHelper.getListenerCount());
		listHelper.add(listener2);
		assertEquals(2, listHelper.getListenerCount());
		listHelper.add(listener3);
		assertEquals(3, listHelper.getListenerCount());
		listHelper.remove(listener1);
		assertEquals(2, listHelper.getListenerCount());
		listHelper.remove(listener2);
		assertEquals(1, listHelper.getListenerCount());
		listHelper.remove(listener3);
		assertEquals(0, listHelper.getListenerCount());

	}

	/**
	 * Test method for {@link EventListenerListHelper#getListeners()}.
	 */
	@Test
	public void testGetListeners() {

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);
		Object listener1 = new Object();
		Object listener2 = new Object();

		listHelper.addAll(new Object[] { listener1, listener2 });

		Object[] listeners = listHelper.getListeners();
		assertEquals(2, listeners.length);
		assertEquals(listener1, listeners[0]);
		assertEquals(listener2, listeners[1]);

	}

	/**
	 * Test method for {@link EventListenerListHelper#iterator()}.
	 */
	@Test
	public void testIterator() {

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);
		Object listener1 = new Object();
		Object listener2 = new Object();
		Iterator itr = listHelper.iterator();

		assertFalse(itr.hasNext(), "Assert iterator.hasNext() returns false");
		listHelper.add(listener1);
		listHelper.add(listener2);
		assertFalse(itr.hasNext(), "Assert iterator.hasNext() returns false");
		itr = listHelper.iterator();
		assertTrue(itr.hasNext(), "Assert iterator.hasNext() returns true");
		assertEquals(listener1, itr.next());
		assertTrue(itr.hasNext(), "Assert iterator.hasNext() returns true");
		assertEquals(listener2, itr.next());
		assertFalse(itr.hasNext(), "Assert iterator.hasNext() returns false");

		try {
			itr.next();
			fail("Should have thrown a NoSuchElementException");
		} catch (NoSuchElementException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Test method for {@link EventListenerListHelper#fire(java.lang.String)}.
	 */
	@Test
	public void testFireByMethodName() {

		EventListenerListHelper listHelper = new EventListenerListHelper(DummyEventListener.class);

		DummyEventListener listener1 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		DummyEventListener listener2 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		listener1.onEvent1();
		listener2.onEvent1();
		EasyMock.replay(listener1);
		EasyMock.replay(listener2);

		listHelper.add(listener1);
		// if listener is added to list helper twice, should still only receive
		// one event notification
		listHelper.add(listener1);
		listHelper.add(listener2);

		listHelper.fire("onEvent1");

		try {
			listHelper.fire(null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		try {
			listHelper.fire("bogusEventName");
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		EasyMock.verify(listener1);
		EasyMock.verify(listener2);

	}

	/**
	 * Test method for
	 * {@link EventListenerListHelper#fire(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testFireByMethodNameWithOneArg() {

		EventListenerListHelper listHelper = new EventListenerListHelper(DummyEventListener.class);
		String arg1 = "arg1";

		DummyEventListener listener1 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		DummyEventListener listener2 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		listener1.onEvent2(arg1);
		listener1.onEvent2(null);
		listener2.onEvent2(arg1);
		listener2.onEvent2(null);
		EasyMock.replay(listener1);
		EasyMock.replay(listener2);

		listHelper.add(listener1);
		// if listener is added to list helper twice, should still only receive
		// one event notification
		listHelper.add(listener1);
		listHelper.add(listener2);

		listHelper.fire("onEvent2", arg1);
		listHelper.fire("onEvent2", (Object) null);

		try {
			listHelper.fire("bogusEventName", arg1);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		try {
			listHelper.fire(null, arg1);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		EasyMock.verify(listener1);
		EasyMock.verify(listener2);

	}

	/**
	 * Test method for
	 * {@link EventListenerListHelper#fire(java.lang.String, java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public void testFireByMethodNameWithTwoArgs() {

		EventListenerListHelper listHelper = new EventListenerListHelper(DummyEventListener.class);
		String arg1 = "arg1";
		String arg2 = "arg2";

		DummyEventListener listener1 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		DummyEventListener listener2 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		listener1.onEvent3(arg1, arg2);
		listener1.onEvent3(null, null);
		listener2.onEvent3(null, null);
		listener2.onEvent3(arg1, arg2);
		EasyMock.replay(listener1);
		EasyMock.replay(listener2);

		listHelper.add(listener1);
		// if listener is added to list helper twice, should still only receive
		// one event notification
		listHelper.add(listener1);
		listHelper.add(listener2);

		listHelper.fire("onEvent3", arg1, arg2);
		listHelper.fire("onEvent3", null, null);

		try {
			listHelper.fire("bogusEventName", arg1, arg2);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		try {
			listHelper.fire(null, arg1, arg2);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		EasyMock.verify(listener1);
		EasyMock.verify(listener2);

	}

	/**
	 * Test method for
	 * {@link org.springframework.richclient.util.EventListenerListHelper#fire(java.lang.String, java.lang.Object[])}.
	 */
	@Test
	public void testFireByMethodNameWithArrayArg() {

		EventListenerListHelper listHelper = new EventListenerListHelper(DummyEventListener.class);
		String arg1 = "arg1";
		String arg2 = "arg2";
		String arg3 = "arg3";

		Object[] args = new Object[] { arg1, arg2, arg3 };

		DummyEventListener listener1 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		DummyEventListener listener2 = (DummyEventListener) EasyMock.createMock(DummyEventListener.class);
		listener1.onEvent4(args);
		listener1.onEvent4(null);
		listener2.onEvent4(args);
		listener2.onEvent4(null);
		EasyMock.replay(listener1);
		EasyMock.replay(listener2);

		listHelper.add(listener1);
		// if listener is added to list helper twice, should still only receive
		// one event notification
		listHelper.add(listener1);
		listHelper.add(listener2);

		// The cast to Object here, and below, is a workaround for varargs
		// conversion in Java 5
		listHelper.fire("onEvent4", (Object) args);
		listHelper.fire("onEvent4", (Object) null);

		try {
			listHelper.fire("bogusEventName", (Object) args);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		try {
			listHelper.fire(null, (Object) args);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing
		}

		EasyMock.verify(listener1);
		EasyMock.verify(listener2);

	}

	/**
	 * Test method for {@link EventListenerListHelper#add(java.lang.Object)}.
	 */
	@Test
	public void testAdd() {

		EventListenerListHelper listHelper = new EventListenerListHelper(String.class);

		assertFalse(listHelper.add(null), "Assert adding a null listener returns false");
		String listener1 = "bogusListener";
		assertTrue(listHelper.add(listener1), "Assert adding a new listener returns true");
		assertFalse(listHelper.add(listener1), "Assert adding an existing listener returns false");

		try {
			listHelper.add(new Object());
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Test method for
	 * {@link org.springframework.richclient.util.EventListenerListHelper#addAll(java.lang.Object[])}.
	 */
	@Test
	public void testAddAll() {

		EventListenerListHelper listHelper = new EventListenerListHelper(String.class);

		assertFalse(listHelper.addAll(null), "Assert adding a null array of listeners returns false");

		String listener1 = "listener1";
		String listener2 = "listener2";
		String[] listenerArray = new String[] { listener1, listener2 };

		assertTrue(listHelper.addAll(listenerArray), "Assert adding an array of new listeners returns true");
		assertFalse(listHelper.addAll(listenerArray), "Assert adding same listeners returns false");

		String[] listenerArray2 = new String[] { "newListener", listener1 };

		assertTrue(listHelper.addAll(listenerArray2), "Assert adding array with one new listener returns true");

		Object[] listenerArray3 = new Object[] { listener1, new Object() };

		try {
			listHelper.addAll(listenerArray3);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Test method for {@link EventListenerListHelper#remove(java.lang.Object)}.
	 */
	@Test
	public void testRemove() {

		EventListenerListHelper listHelper = new EventListenerListHelper(String.class);

		String listener1 = "listener1";

		listHelper.add(listener1);

		assertEquals(1, listHelper.getListenerCount());

		listHelper.remove("bogusListener");

		assertEquals(1, listHelper.getListenerCount());

		listHelper.remove(listener1);

		assertEquals(0, listHelper.getListenerCount());

		try {
			listHelper.remove(new Object());
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

		// TODO why does this method need to throw an IllegalArgEx?
		try {
			listHelper.remove(null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// do nothing, test succeeded
		}

	}

	/**
	 * Test method for
	 * {@link org.springframework.richclient.util.EventListenerListHelper#clear()}.
	 */
	@Test
	public void testClear() {

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);

		listHelper.clear();

		assertEquals(0, listHelper.getListenerCount());
		listHelper.add(new Object());
		assertEquals(1, listHelper.getListenerCount());
		listHelper.clear();
		assertEquals(0, listHelper.getListenerCount());

	}

	/**
	 * Test method for
	 * {@link org.springframework.richclient.util.EventListenerListHelper#toArray()}.
	 */
	@Test
	public void testToArray() {

		Object listener1 = new Object();
		Object listener2 = new Object();

		Object[] listenerArray = new Object[] { listener1, listener2 };

		EventListenerListHelper listHelper = new EventListenerListHelper(Object.class);

		listHelper.addAll(listenerArray);

		Object[] listenersCopy = (Object[]) listHelper.toArray();

		assertEquals(listenerArray.length, listenersCopy.length);

		for (int i = 0; i < listenerArray.length; i++) {
			assertEquals(listenerArray[i], listenersCopy[i]);
		}

	}

	private interface DummyEventListener {

		public void onEvent1();

		public void onEvent2(Object arg1);

		public void onEvent3(Object arg1, Object arg2);

		public void onEvent4(Object[] args);

	}

}
