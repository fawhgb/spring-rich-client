/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.swing.Icon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.form.FieldFace;
import org.springframework.binding.form.FormModel;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.ReflectiveVisitorHelper;
import org.springframework.richclient.application.ApplicationServices;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.core.LabelInfo;
import org.springframework.richclient.image.IconSource;

/**
 * An implementation of FieldFaceSource that resolves the FieldFace from the
 * <code>MessageSourceAccessor</code> provided to the setMessageSourceAccessor
 * method or from the {@link ApplicationServices} singleton if none is provided.
 *
 * <p>
 * The various properties of the FieldFace are resolved from a message source
 * using message codes. These codes where generated by a
 * {@link MessageCodeStrategy}. If no other {@link MessageCodeStrategy} is
 * defined an instance of {@link DefaultMessageCodeStrategy} will be used
 *
 * @author Oliver Hutchison
 * @author Mathias Broekelmann
 */
public class MessageSourceFieldFaceSource extends CachingFieldFaceSource {

	private static final Log log = LogFactory.getLog(MessageSourceFieldFaceSource.class);

	/**
	 * Name for the FieldFace's <code>displayName</code> property.
	 */
	private static final String[] DISPLAY_NAME_PROPERTY = { "displayName" };

	/**
	 * Name for the FieldFace's <code>caption</code> property.
	 */
	private static final String[] CAPTION_PROPERTY = { "caption" };

	/**
	 * Name for the FieldFace's <code>description</code> property.
	 */
	private static final String[] DESCRIPTION_PROPERTY = { "description" };

	/**
	 * Name for the FieldFace's <code>labelInfo</code> property.
	 */
	private static final String[] ENCODED_LABEL_PROPERTY = { "label", "" };

	/**
	 * Name for the FieldFace's <code>icon</code> property.
	 */
	private static final String[] ICON_PROPERTY = { "icon" };

	private MessageSourceAccessor messageSourceAccessor;

	private MessageCodeStrategy messageKeyStrategy;

	private IconSource iconSource;

	private final ReflectiveVisitorHelper visitorHelper = new ReflectiveVisitorHelper();

	/**
	 * Constructs a new MessageSourcePropertyFaceDescriptorSource.
	 */
	public MessageSourceFieldFaceSource() {
	}

	/**
	 * Set the message source that will be used to resolve the FieldFace's
	 * properties.
	 */
	public void setMessageSourceAccessor(MessageSourceAccessor messageSourceAccessor) {
		this.messageSourceAccessor = messageSourceAccessor;
	}

	/**
	 * If a message source was provided to the setMessageSourceAccessor method
	 * returns that otherwise returns the default message source located using the
	 * {@link ApplicationServices} singleton
	 */
	protected MessageSourceAccessor getMessageSourceAccessor() {
		if (messageSourceAccessor == null) {
			messageSourceAccessor = (MessageSourceAccessor) ApplicationServicesLocator.services()
					.getService(MessageSourceAccessor.class);
		}
		return messageSourceAccessor;
	}

	/**
	 * Set the icon source that will be used to resolve the FieldFace's icon
	 * property.
	 */
	public void setIconSource(IconSource iconSource) {
		this.iconSource = iconSource;
	}

	protected IconSource getIconSource() {
		if (iconSource == null) {
			iconSource = (IconSource) ApplicationServicesLocator.services().getService(IconSource.class);
		}
		return iconSource;
	}

	/**
	 * Returns the value of the required property of the FieldFace. Delegates to the
	 * getMessageKeys for the message key generation strategy. This method uses
	 * </code>[contextId + "." + ] fieldPath [ + "." +
	 * faceDescriptorProperty[0]]</code> for the default value
	 */
	protected String getMessage(String contextId, String fieldPath, String[] faceDescriptorProperty) {
		String[] keys = getMessageKeys(contextId, fieldPath, faceDescriptorProperty);
		return getMessageSourceAccessor().getMessage(new DefaultMessageSourceResolvable(keys, null, keys[0]));
	}

	/**
	 * Returns the value of the required property of the FieldFace. Delegates to the
	 * getMessageKeys for the message key generation strategy.
	 */
	protected String getMessage(String contextId, String fieldPath, String[] faceDescriptorProperties,
			String defaultValue) {
		String[] keys = getMessageKeys(contextId, fieldPath, faceDescriptorProperties);
		try {
			return getMessageSourceAccessor().getMessage(new DefaultMessageSourceResolvable(keys, null, defaultValue));
		} catch (NoSuchMessageException e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage());
			}
			return null;
		}
	}

	/**
	 * Returns an array of message keys that are used to resolve the required
	 * property of the FieldFace. The property will be resolved from the message
	 * source using the returned message keys in order.
	 * <p>
	 * Subclasses my override this method to provide an alternative to the default
	 * message key generation strategy.
	 */
	protected String[] getMessageKeys(String contextId, String fieldPath, String[] faceDescriptorProperties) {
		return getMessageKeyStrategy().getMessageCodes(contextId, fieldPath, faceDescriptorProperties);
	}

	protected FieldFace loadFieldFace(String field, String contextId) {
		String caption = getMessage(contextId, field, CAPTION_PROPERTY, null);
		String description = getMessage(contextId, field, DESCRIPTION_PROPERTY, null);
		String encodedLabel = getMessage(contextId, field, ENCODED_LABEL_PROPERTY);
		if (encodedLabel == null) {
			// try loading the default value
			encodedLabel = getMessage(contextId, field, null);
		}
		String iconName = getMessage(contextId, field, ICON_PROPERTY, null);
		Icon icon = null;
		if (iconName != null) {
			icon = getIconSource().getIcon(iconName);
		}
		LabelInfo labelInfo = LabelInfo.valueOf(encodedLabel);
		String displayName = getMessage(contextId, field, DISPLAY_NAME_PROPERTY, labelInfo.getText());
		return new DefaultFieldFace(displayName, caption, description, labelInfo, icon);
	}

	@Override
	protected FieldFace loadFieldFace(String field, Object context) {
		String contextId = (String) visitorHelper.invokeVisit(this, context);
		return loadFieldFace(field, contextId);
	}

	public MessageCodeStrategy getMessageKeyStrategy() {
		if (messageKeyStrategy == null) {
			messageKeyStrategy = new DefaultMessageCodeStrategy();
		}
		return messageKeyStrategy;
	}

	public void setMessageKeyStrategy(MessageCodeStrategy messageKeyStrategy) {
		this.messageKeyStrategy = messageKeyStrategy;
	}

	// visit methods for getting a context id from various context instances

	String visit(FormModel formModel) {
		return formModel.getId();
	}

	String visit(CharSequence contextId) {
		return contextId.toString();
	}

	String visitNull() {
		return null;
	}
}