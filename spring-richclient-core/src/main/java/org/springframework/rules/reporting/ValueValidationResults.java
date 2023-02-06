package org.springframework.rules.reporting;

import org.springframework.richclient.core.Severity;
import org.springframework.rules.constraint.Constraint;

/**
 * @author Keith Donald
 */
public class ValueValidationResults implements ValidationResults {

	private Object argument;

	private Constraint violatedConstraint;

	public ValueValidationResults(Object argument, Constraint violatedConstraint) {
		this.argument = argument;
		this.violatedConstraint = violatedConstraint;
	}

	public ValueValidationResults(Object argument) {
		this.argument = argument;
	}

	/**
	 * @see org.springframework.rules.reporting.ValidationResults#getRejectedValue()
	 */
	@Override
	public Object getRejectedValue() {
		return argument;
	}

	/**
	 * @see org.springframework.rules.reporting.ValidationResults#getViolatedConstraint()
	 */
	@Override
	public Constraint getViolatedConstraint() {
		return violatedConstraint;
	}

	/**
	 * @see org.springframework.rules.reporting.ValidationResults#getViolatedCount()
	 */
	@Override
	public int getViolatedCount() {
		if (violatedConstraint != null) {
			return new SummingVisitor(violatedConstraint).sum();
		}

		return 0;
	}

	/**
	 * @see org.springframework.rules.reporting.ValidationResults#getSeverity()
	 */
	@Override
	public Severity getSeverity() {
		return Severity.ERROR;
	}

}
