package org.springframework.richclient.context.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ApplicationWindowScope implements Scope {

	public ApplicationWindowScope() {
		System.out.println("ttt");
	}

	@Override
	public Object get(String name, ObjectFactory objectFactory) {
		return objectFactory.getObject();
	}

	@Override
	public String getConversationId() {
		return "ttt";
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {

	}

	@Override
	public Object remove(String name) {
		return null;
	}

}
