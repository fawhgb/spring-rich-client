/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.binding.validation.support.DefaultValidationMessage;
import org.springframework.richclient.core.Severity;

public class DefaultValidationMessageTests {

	@Test
	public void testDefaultValidationMessage() {
		ValidationMessage vm = new DefaultValidationMessage("property", Severity.INFO, "message");
		assertEquals("property", vm.getProperty());
		assertEquals(Severity.INFO, vm.getSeverity());
		assertEquals("message", vm.getMessage());
	}

	@Test
	public void testToString() {
		ValidationMessage vm = new DefaultValidationMessage("property", Severity.INFO, "message");
		assertTrue(vm.toString().endsWith("property = \'property\', severity = \'info\', message = \'message\']"));
	}
}
