/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.core.closure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.rules.closure.ElementGenerator;
import org.springframework.rules.closure.support.Block;
import org.springframework.rules.closure.support.IteratorTemplate;
import org.springframework.rules.constraint.Constraint;

/**
 * @author Keith Donald
 */
public class ClosureTests {

	@Test
	public void testIteratorProcessTemplateRunOnce() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection.iterator());
		assertTrue(template.allTrue(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Item");
			}
		}));
		try {
			assertEquals(false, template.allTrue(new Constraint() {
				@Override
				public boolean test(Object o) {
					return ((String) o).startsWith("Element");
				}
			}));
			fail("Should have failed");
		} catch (UnsupportedOperationException e) {

		}
	}

	@Test
	public void testIteratorProcessTemplateRunMultiple() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection);
		assertTrue(template.allTrue(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Item");
			}
		}));
		assertEquals(false, template.allTrue(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Element");
			}
		}));
	}

	@Test
	public void testAnyTrue() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection);
		assertTrue(template.anyTrue(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Item 5");
			}
		}));
		assertEquals(false, template.anyTrue(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Element");
			}
		}));
	}

	@Test
	public void testFindFirst() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection);
		assertEquals("Item 4", template.findFirst(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Item 4");
			}
		}));
		assertEquals(null, template.findFirst(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Element");
			}
		}));
	}

	@Test
	public void testFindAll() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection);
		ElementGenerator finder = template.findAll(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Item 4");
			}
		});
		finder.run(new Block() {
			@Override
			protected void handle(Object o) {
				assertEquals("Item 4", o);
			}
		});
		finder = template.findAll(new Constraint() {
			@Override
			public boolean test(Object o) {
				return ((String) o).startsWith("Element");
			}
		});
		finder.run(new Block() {
			@Override
			protected void handle(Object o) {
				fail("Should not be called");
			}
		});
	}

	// using a simple int instead of a ValueHolder to have no extra dependencies.
	int runUntilCounter;

	@Test
	public void testRunUntil() {
		List collection = new ArrayList();
		collection.add("Item 1");
		collection.add("Item 2");
		collection.add("Item 3");
		collection.add("Item 4");
		collection.add("Item 5");
		IteratorTemplate template = new IteratorTemplate(collection);
		runUntilCounter = 0;
		template.runUntil(new Block() {
			@Override
			protected void handle(Object o) {
				runUntilCounter++;
			}
		}, new Constraint() {
			@Override
			public boolean test(Object o) {
				return o.equals("Item 4");
			}
		});
		assertEquals(3, runUntilCounter);
	}
}