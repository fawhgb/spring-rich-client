package org.springframework.richclient.form.builder.support;

import org.springframework.binding.form.FormModel;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.FormComponentInterceptorFactory;

public class PromptTextFieldFormComponentInterceptorFactory
		implements FormComponentInterceptorFactory, MessageSourceAware {
	private MessageSource messageSource;

	private String promptKey;

	public String getPromptKey() {
		return promptKey;
	}

	public void setPromptKey(String promptKey) {
		this.promptKey = promptKey;
	}

	@Override
	public FormComponentInterceptor getInterceptor(FormModel formModel) {
		PromptTextFieldFormComponentInterceptor interceptor = new PromptTextFieldFormComponentInterceptor(formModel,
				messageSource);
		interceptor.setPromptKey(getPromptKey());
		return interceptor;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
