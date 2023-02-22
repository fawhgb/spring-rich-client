/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.rules.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.rules.Rules;

/**
 * @author Mathias Broekelmann
 * 
 */
public class DefaultRulesSourceTests {

	private DefaultRulesSource source;

	private Rules interfaceRules;

	@BeforeEach
	protected void setUp() throws Exception {
		source = new DefaultRulesSource();
		interfaceRules = new Rules(TestInterface.class);
	}

	@AfterEach
	protected void tearDown() throws Exception {
		source = null;
		interfaceRules = null;
	}

	@Test
	public void testInterfaceRules() {
		source.addRules(interfaceRules);
		assertEquals(interfaceRules, source.getRules(TestInterfaceImpl.class));
	}

	private static interface TestInterface {
	}

	private static class TestInterfaceImpl implements TestInterface {
	}

}
