package validation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dao.OrderDao;
import dao.OrderItemDao;
import dao.OrderItemStatus;
import dao.ResponseFileStatus;
import parser.CsvParser;

public class OrderValidator {

	private static OrderValidator instance = null;

	private Set<String> registeredOrderIdsFromThisFile = new HashSet<>();
	
	private ResponseOfValidation responseOfValidation;

	public static OrderValidator getInstance() {
		synchronized (OrderValidator.class) {
			if (instance == null)
				instance = new OrderValidator();
			return instance;
		}
	}

	private OrderValidator() {

	}

	private ResponseOfValidation addErrorAndMessage(String message) {
		responseOfValidation.setResponseFileStatus(ResponseFileStatus.ERROR);
		responseOfValidation.setMessage(message);
		return responseOfValidation;
	}
	
	// TODO vizsgálat üres fieldre
	// TODO date lekezelése (ami lehet üres)
	// TODO előkészíteni az adatokat insert-re
	// TODO kitesztelni a db-ből való ellenőrzést
	// TODO insertek megírása
	
	public ResponseOfValidation validateLine(String[] csvFields, int indexOfRow) {
		final String messagePrefix = "Line with LineNumber " + csvFields[CsvParser.INDEX_OF_LINENUMBER] + " (Rowindex in file: "
				+ indexOfRow + ") ";
		responseOfValidation = new ResponseOfValidation(csvFields[CsvParser.INDEX_OF_LINENUMBER]);
		
		if (csvFields.length < CsvParser.CSV_NUMBER_OF_FIELDS) {
			return addErrorAndMessage(messagePrefix + "has unsufficient number of fields.");
		}
		// check the db here
		try {
			if(registeredOrderIdsFromThisFile.contains(csvFields[CsvParser.INDEX_OF_ORDERID])) {
				responseOfValidation.setOrderShouldBeInserted(false);
			} else {
				if(!validateOrderId(csvFields[CsvParser.INDEX_OF_ORDERID])) {
					return addErrorAndMessage(messagePrefix + " has an orderItemId, that has been already registered or its format is invalid.");
				}
			}
			if(!validateOrderItemId(csvFields[CsvParser.INDEX_OF_ORDERITEMID])) {
				return addErrorAndMessage(messagePrefix + " has an orderId, that has been already registered or its format is invalid.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return addErrorAndMessage(messagePrefix + "an error accured when trying to check if orderItem already existed in the database.");
		}
		if (!validateEmailFormat(csvFields[CsvParser.INDEX_OF_BUYEREMAIL])) {
			return addErrorAndMessage(messagePrefix + "has invalid buyerEmail format.");
		}
		if (!validateDateFormat(csvFields[CsvParser.INDEX_OF_ORDERDATE])) {
			return addErrorAndMessage(messagePrefix + "has invalid orderDate format.");
		}
		if (!validateInteger(csvFields[CsvParser.INDEX_OF_POSTCODE])) {
			return addErrorAndMessage(messagePrefix + "has invalid postCode format.");
		}
		if(!validatePositiveDecimal(csvFields[CsvParser.INDEX_OF_SHIPPINGPRICE], 0.00f)) { // TODO külön vizsgáljak értékre és formatra?
			return addErrorAndMessage(messagePrefix + "has invalid shippingPrice format.");
		}
		if(!validatePositiveDecimal(csvFields[CsvParser.INDEX_OF_SALEPRICE], 1.00f)) { // TODO külön vizsgáljak értékre és formatra?
			return addErrorAndMessage(messagePrefix + "has invalid salePrice format.");
		}
		if(!validateStatus(csvFields[CsvParser.INDEX_OF_STATUS])) {
			return addErrorAndMessage(messagePrefix + "has invalid status.");
		}
		responseOfValidation.setResponseFileStatus(ResponseFileStatus.OK);
		return responseOfValidation;
	}

	public boolean validateEmailFormat(String emailAddress) {
		final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
		final Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(emailAddress);
		return matcher.matches();
	}

	public boolean validateDateFormat(String orderDate) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(orderDate);
			if (!orderDate.equals(sdf.format(date))) {
				return false;
			}
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public boolean validateInteger(String number) {
		try {
	        Integer.parseInt(number);
	        return true;
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	}
	
	public boolean validatePositiveDecimal(String number, float min) {
		try {
	        if(Float.parseFloat(number) >= min) {	        	
	        	return true;
	        }
	        return false;
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	}
	
	public boolean validateStatus(String status) {
		try {
			OrderItemStatus.valueOf(status);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public boolean validateOrderId(String orderId) throws SQLException {
		Long id = longParser(orderId);
		if(id == null) {
			return false;
		}
		OrderDao orderDao = OrderDao.getInstance();
		ResultSet rs = orderDao.findById(id);
		boolean notFound = !rs.isBeforeFirst(); 
		
		if(notFound) {
			registeredOrderIdsFromThisFile.add(orderId);
		}

		return notFound;
	}
	
	public boolean validateOrderItemId(String orderItemId) throws SQLException {
		Long id = longParser(orderItemId);
		if(id == null) {
			return false;
		}
		OrderItemDao orderItemDao = OrderItemDao.getInstance();
		ResultSet rs = orderItemDao.findById(id);
		
		return !rs.isBeforeFirst();
	}
	
	public Long longParser(String orderId) {
		try {
			return Long.parseLong(orderId);
		} catch (NumberFormatException ex) {
	        return null;
	    }
	}
	
}
