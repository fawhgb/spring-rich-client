/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.richclient.form.binding.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Implementation of {@link ListDataListener} that eases testing of Swing's list
 * related classes.
 * 
 * @author Oliver Hutchison
 */
public class TestListDataListener implements ListDataListener {
	private ListDataEvent lastEvent;

	private int calls;

	public void assertCalls(int calls) {
		assertEquals(calls, this.calls, "ListDataListener has not been called expected number of times.");
	}

	public void assertEvent(int calls, int eventType, int index0, int index1) {
		assertCalls(calls);
		assertEquals(eventType, lastEvent.getType(), "Last ListDataEvent has unexpected type.");
		assertEquals(index0, lastEvent.getIndex0(), "Last ListDataEvent has unexpected index0.");
		assertEquals(index1, lastEvent.getIndex1(), "Last ListDataEvent has unexpected index1.");
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		calls++;
		lastEvent = e;
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		calls++;
		lastEvent = e;
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		calls++;
		lastEvent = e;
	}
}