package org.springframework.binding.format.support;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;

/**
 * This is a decorator class for NumberFormat to ensure an exact number parsing.
 * The {@link NumberFormat} class allows parsing of numbers in strings like
 * '2abc' but at the richclient end we don't want this to be a valid parsing.
 * Therefor a specific NumberFormat that doesn't allow any other input than a
 * number.
 *
 * @author Yudhi Widyatama
 * @author Jan Hoskens
 *
 */
public class StrictNumberFormat extends NumberFormat {
	private static final long serialVersionUID = 1L;
	NumberFormat inner;

	public StrictNumberFormat(NumberFormat instance) {
		inner = instance;
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return inner.format(number, toAppendTo, pos);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return inner.format(number, toAppendTo, pos);
	}

	@Override
	public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
		return inner.format(number, toAppendTo, pos);
	}

	@Override
	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		return inner.formatToCharacterIterator(obj);
	}

	@Override
	public Currency getCurrency() {
		return inner.getCurrency();
	}

	@Override
	public int getMaximumFractionDigits() {
		return inner.getMaximumFractionDigits();
	}

	@Override
	public int getMaximumIntegerDigits() {
		return inner.getMaximumIntegerDigits();
	}

	@Override
	public int getMinimumFractionDigits() {
		return inner.getMinimumFractionDigits();
	}

	@Override
	public int getMinimumIntegerDigits() {
		return inner.getMinimumIntegerDigits();
	}

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	@Override
	public boolean isGroupingUsed() {
		return inner.isGroupingUsed();
	}

	@Override
	public boolean isParseIntegerOnly() {
		return inner.isParseIntegerOnly();
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		return inner.parse(source, parsePosition);
	}

	@Override
	public Number parse(String source) throws ParseException {
		// idea taken from
		// org.apache.commons.validator.routines.AbstractFormatValidator
		ParsePosition parsePosition = new ParsePosition(0);
		Number result = inner.parse(source, parsePosition);
		if (parsePosition.getErrorIndex() > -1) {
			throw new ParseException("Invalid format", parsePosition.getIndex());
		}
		if (parsePosition.getIndex() < source.length()) {
			throw new ParseException("Invalid format[ii]", parsePosition.getIndex());
		}
		return result;
	}

	@Override
	public Object parseObject(String source) throws ParseException {
		return inner.parseObject(source);
	}

	@Override
	public void setCurrency(Currency currency) {
		inner.setCurrency(currency);
	}

	@Override
	public void setGroupingUsed(boolean newValue) {
		inner.setGroupingUsed(newValue);
	}

	@Override
	public void setMaximumFractionDigits(int newValue) {
		inner.setMaximumFractionDigits(newValue);
	}

	@Override
	public void setMaximumIntegerDigits(int newValue) {
		inner.setMaximumIntegerDigits(newValue);
	}

	@Override
	public void setMinimumFractionDigits(int newValue) {
		inner.setMinimumFractionDigits(newValue);
	}

	@Override
	public void setMinimumIntegerDigits(int newValue) {
		inner.setMinimumIntegerDigits(newValue);
	}

	@Override
	public void setParseIntegerOnly(boolean value) {
		inner.setParseIntegerOnly(value);
	}
}
