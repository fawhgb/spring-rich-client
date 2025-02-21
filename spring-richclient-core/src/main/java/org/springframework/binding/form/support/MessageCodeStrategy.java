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
package org.springframework.binding.form.support;

/**
 * MessageKeyStrategy is used by {@link MessageSourceFieldFaceSource} to create
 * the codes for resolving messages.
 *
 * @author Mathias Broekelmann
 *
 */
public interface MessageCodeStrategy {

	/**
	 * Creates message codes.
	 * 
	 * @param contextId optional contextId of the field.
	 * @param field     the field. The field name
	 * @param suffixes  optional array of suffixes.
	 * @return an array of message codes
	 */
	String[] getMessageCodes(String contextId, String field, String[] suffixes);

}
