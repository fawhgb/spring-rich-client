/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.richclient.application.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.View;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.config.CommandButtonLabelInfo;
import org.springframework.richclient.command.support.ShowViewCommand;
import org.springframework.richclient.core.LabeledObjectSupport;
import org.springframework.util.Assert;

/**
 * Provides a standard implementation of {@link ViewDescriptor}.
 *
 * @author Keith Donald
 */
public class DefaultViewDescriptor extends LabeledObjectSupport
		implements ViewDescriptor, BeanNameAware, InitializingBean {
	private String id;

	private Class<? extends View> viewClass;

	private Map<String, Object> viewProperties;

	public DefaultViewDescriptor() {
		// default constructor for spring creation
	}

	public DefaultViewDescriptor(String id, Class<? extends View> viewClass) {
		this(id, viewClass, Collections.<String, Object>emptyMap());
	}

	public DefaultViewDescriptor(String id, Class<? extends View> viewClass, Map<String, Object> viewProperties) {
		setId(id);
		setViewClass(viewClass);
		setViewProperties(viewProperties);
	}

	@Override
	public void setBeanName(String beanName) {
		setId(beanName);
	}

	public void setId(String id) {
		Assert.notNull("id is required");
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	public Class<? extends View> getViewClass() {
		return viewClass;
	}

	public void setViewClass(Class<? extends View> viewClass) {
		Assert.notNull(viewClass, "viewClass cannot be null");
		Assert.isTrue(View.class.isAssignableFrom(viewClass), "viewClass doesn't derive from View");

		this.viewClass = viewClass;
	}

	public void setViewProperties(Map<String, Object> viewProperties) {
		this.viewProperties = viewProperties;
	}

	@Override
	public PageComponent createPageComponent() {
		return createView();
	}

	protected View createView() {
		Assert.state(viewClass != null, "View class to instantiate is not set");
		Object o = BeanUtils.instantiateClass(viewClass);
		Assert.isTrue((o instanceof View),
				"View class '" + viewClass + "' was instantiated, but instance is not a View!");
		View view = (View) o;
		view.setDescriptor(this);
		if (viewProperties != null) {
			BeanWrapper wrapper = new BeanWrapperImpl(view);
			wrapper.setPropertyValues(viewProperties);
		}

		if (view instanceof InitializingBean) {
			try {
				((InitializingBean) view).afterPropertiesSet();
			} catch (Exception e) {
				throw new BeanInitializationException("Problem running on " + view, e);
			}
		}
		return view;
	}

	@Override
	public CommandButtonLabelInfo getShowViewCommandLabel() {
		return getLabel();
	}

	@Override
	public ActionCommand createShowViewCommand(ApplicationWindow window) {
		return new ShowViewCommand(this, window);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(id, "id is mandatory");
		Assert.notNull(viewClass, "viewClass is mandatory");
	}

}