/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.rules.constraint;

/**
 * Always returns true; a wildcard match
 *
 * @author Keith Donald
 */
public class WildcardConstraint extends AbstractConstraint {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean test(Object argument) {
		return true;
	}
}
