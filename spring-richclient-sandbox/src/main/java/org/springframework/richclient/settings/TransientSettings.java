/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.richclient.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class, essential when testing with settings. Completely functional
 * <code>Settings</code> implementation, but the settings values are not stored
 * in any backing store.
 *
 * @author Peter De Bruycker
 */
public class TransientSettings extends AbstractSettings {

	private Map values = new HashMap();

	public TransientSettings() {
		this(null, "");
	}

	public TransientSettings(TransientSettings parent, String name) {
		super(parent, name);
	}

	@Override
	protected void internalSet(String key, String value) {
		values.put(key, value);
	}

	@Override
	protected String internalGet(String key) {
		return (String) values.get(key);
	}

	@Override
	public String[] getKeys() {
		return (String[]) values.keySet().toArray(new String[0]);
	}

	@Override
	public void save() throws IOException {
	}

	@Override
	public void load() throws IOException {
	}

	@Override
	protected Settings internalCreateChild(String key) {
		return new TransientSettings(this, key);
	}

	@Override
	protected void internalRemove(String key) {
		values.remove(key);
	}

	@Override
	protected boolean internalContains(String key) {
		return values.containsKey(key);
	}

	@Override
	protected String[] internalGetChildSettings() {
		return new String[0];
	}

	@Override
	public void internalRemoveSettings() {

	}
}