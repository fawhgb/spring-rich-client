/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.richclient.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JButton;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiTask;
import org.junit.jupiter.api.Test;

/**
 * Testcase for <code>MenuItemButtonConfigurer</code>.
 * 
 * @author Peter De Bruycker
 */
public class MenuItemButtonConfigurerTests extends CommandButtonConfigurerTestCase {

	@Override
	protected CommandButtonConfigurer createConfigurer() {
		return new DefaultCommandButtonConfigurer();
	}

	@Test
	public void testConfigure() {
		GuiActionRunner.execute(new GuiTask() {
			@Override
			protected void executeInEDT() throws Throwable {
				MenuItemButtonConfigurer configurer = new MenuItemButtonConfigurer();
				JButton button = new JButton();

				configurer.configure(button, null, getCommandFaceDescriptor());

				assertEquals(getCommandFaceDescriptor().getText(), button.getText());
				assertEquals(getCommandFaceDescriptor().getIcon(), button.getIcon());
				assertEquals(null, button.getToolTipText());
			}
		});
	}
}