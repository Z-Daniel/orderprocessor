package com.zsidodaniel.orderprocesser.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import validation.OrderValidator;

public class OrderValidatorTest {

	OrderValidator orderValidator = OrderValidator.getInstance();

	@Test
	public void emailFormatValidationShouldFail() {

		String[] invalidEmails = { "journaldev", "journaldev@.com.my", "journaldev123@gmail.a",
				"journaldev123@@.com.com", ".journaldev@journaldev.com", "journaldev()*@gmail.com", "journaldev@%*.com",
				"journaldev..2002@gmail.com", "journaldev.@gmail.com", "journaldev@journaldev@gmail.com",
				"journaldev@gmail.com.1a" };

		for (int i = 0; i < invalidEmails.length; i++) {
			assertFalse(orderValidator.validateEmailFormat(invalidEmails[i]));
		}
	}

	@Test
	public void emailFormatValidationShouldPass() {

		String[] validEmails = { "journaldev@yahoo.com", "journaldev-100@yahoo.com", "journaldev.100@yahoo.com",
				"journaldev111@journaldev.com", "journaldev-100@journaldev.net", "journaldev.100@journaldev.com.au",
				"journaldev@1.com", "journaldev@gmail.com.com", "journaldev+100@gmail.com",
				"journaldev-100@yahoo-test.com", "journaldev_100@yahoo-test.ABC.CoM" };

		for (int i = 0; i < validEmails.length; i++) {
			assertTrue(orderValidator.validateEmailFormat(validEmails[i]));
		}
	}

	@Test
	public void dateValidationShouldFail() throws ParseException {
		String[] invalidDates = { "1993/01/04", "2013.02.01", "1993/01/04 15:22:12", "2013.02.01 14:32:54", "asd",
				"2014-12/01", "01-12-2013", "12-1859-11", "2011-01-34" };

		for (int i = 0; i < invalidDates.length; i++) {
			assertFalse(orderValidator.validateDateFormat(invalidDates[i]));
		}

	}

	@Test
	public void dateValidationShouldPass() {
		String[] validDates = { "1993-01-04", "2013-02-01", "1993-01-04", "2014-12-01", "2013-12-01", "1845-03-01",
				"2011-01-21" };

		for (int i = 0; i < validDates.length; i++) {
			assertTrue(orderValidator.validateDateFormat(validDates[i]));
		}
	}

	@Test
	public void postCodeValidationShouldFail() {
		String[] invalidPostCodes = { "323.12", "asd", "12a3", "123,3", "ad234", "1321d", "2321L" };
		for (int i = 0; i < invalidPostCodes.length; i++) {
			assertFalse(orderValidator.validatePostcode(invalidPostCodes[i]));
		}
	}
	
	@Test
	public void postCodeValidationShouldPass() {
		String[] validPostCodes = { "7831", "7621", "1234", "1233", "5412", "3245", "2321" };
		for (int i = 0; i < validPostCodes.length; i++) {
			assertTrue(orderValidator.validatePostcode(validPostCodes[i]));
		}
	}
	
	@Test
	public void shippingPriceValidationShouldFail() {
		String[] shippingPrices = { "-12.32", "-1", "-12323", "rew", "54.1.2", "3a24ew5", "-232rf2.1" };
		for (int i = 0; i < shippingPrices.length; i++) {
			assertFalse(orderValidator.validatePositiveDecimal(shippingPrices[i], 0.00f, true));
		}
	}
	
	@Test
	public void shippingPriceValidationShouldPass() {
		String[] shippingPrices = { "12.32", "12", "12323.343", "45.3", "0", "0.00", "0.1" };
		for (int i = 0; i < shippingPrices.length; i++) {
			assertTrue(orderValidator.validatePositiveDecimal(shippingPrices[i], 0.00f, true));
		}
	}
	
	@Test
	public void salePriceValidationShouldFail() {
		String[] salePrices = { "0.99", "-12", "-12323.343", "asd", "0", "234r2", "-1.1" };
		for (int i = 0; i < salePrices.length; i++) {
			assertFalse(orderValidator.validatePositiveDecimal(salePrices[i], 1.00f, true));
		}
	}
	
	@Test
	public void salePriceValidationShouldPass() {
		String[] salePrices = { "1.00", "12", "1", "12312.23", "12312", "1.001", "1233" };
		for (int i = 0; i < salePrices.length; i++) {
			assertTrue(orderValidator.validatePositiveDecimal(salePrices[i], 1.00f, true));
		}
	}
	
	@Test
	public void statusValidationShouldFail() {
		String[] states = { "asd", "2", "1", "IN_SPOCK", "OUT_OF_ASD", "12", "3" };
		for (int i = 0; i < states.length; i++) {
			assertFalse(orderValidator.validateStatus(states[i]));
		}
	}
	
	@Test
	public void statusValidationShouldPass() {
		String[] states = { "IN_STOCK", "OUT_OF_STOCK" };
		for (int i = 0; i < states.length; i++) {
			assertTrue(orderValidator.validateStatus(states[i]));
		}
	}
	
	@Test
	public void idFormatValidationShouldFail() {
		String[] ids = { "asd", "a" };
		for (int i = 0; i < ids.length; i++) {
			assertNull(orderValidator.longParser(ids[i]));
		}
	}
	
	@Test
	public void idFormatValidationShouldPass() {
		String[] ids = { "1", "2", "123" };
		assertSame(1L, orderValidator.longParser(ids [0]));
		assertSame(2L, orderValidator.longParser(ids [1]));
		assertSame(123L, orderValidator.longParser(ids [2]));
	}
}
