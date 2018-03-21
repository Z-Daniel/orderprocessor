package validation;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dao.OrderDao;
import dao.OrderItemDao;
import dao.OrderItemStatus;
import dao.ResponseFileStatus;
import parser.CsvParser;

/**
 * Validation of order and orderItem read from csv.
 * @author Zsidó Dániel
 *
 */

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
	
	public ResponseOfValidation validateLine(String[] csvFields) {
		final String messagePrefix = "Line with LineNumber " + csvFields[CsvParser.INDEX_OF_LINENUMBER] + " ";
		responseOfValidation = new ResponseOfValidation(csvFields[CsvParser.INDEX_OF_LINENUMBER]);
		System.out.println("Starting validation of line number " + csvFields[CsvParser.INDEX_OF_LINENUMBER]);
		if (csvFields.length < CsvParser.CSV_NUMBER_OF_FIELDS - 1) {
			System.out.println(csvFields.length);
			return addErrorAndMessage(messagePrefix + "has unsufficient number of fields.");
		}
		try {
			if(registeredOrderIdsFromThisFile.contains(csvFields[CsvParser.INDEX_OF_ORDERID])) {
				responseOfValidation.setOrderShouldBeInserted(false);
				responseOfValidation.setOrderId(longParser(csvFields[CsvParser.INDEX_OF_ORDERID]));
			} else {
				if(csvFields[CsvParser.INDEX_OF_ORDERID].isEmpty() || !validateOrderId(csvFields[CsvParser.INDEX_OF_ORDERID])) {
					return addErrorAndMessage(messagePrefix + " has no orderItemId or it has been already registered or its format is invalid.");
				}
			}
			
			if(csvFields[CsvParser.INDEX_OF_ORDERITEMID].isEmpty() || !validateOrderItemId(csvFields[CsvParser.INDEX_OF_ORDERITEMID])) {
				return addErrorAndMessage(messagePrefix + " has no orderId or it has been already registered or its format is invalid.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return addErrorAndMessage(messagePrefix + "an error accured when trying to check if orderItem already existed in the database.");
		}
		if (csvFields[CsvParser.INDEX_OF_BUYEREMAIL].isEmpty() || !validateEmailFormat(csvFields[CsvParser.INDEX_OF_BUYEREMAIL])) {
			return addErrorAndMessage(messagePrefix + "has no buyerEmail or it its format is invalid.");
		}
		if(csvFields.length == CsvParser.CSV_NUMBER_OF_FIELDS) {
			if (!validateDateFormat(csvFields[CsvParser.INDEX_OF_ORDERDATE])) {
				return addErrorAndMessage(messagePrefix + "has invalid orderDate format.");
			}
		} else {
			responseOfValidation.setOrderDate(new Date(new java.util.Date().getTime()));
		}
		if (csvFields[CsvParser.INDEX_OF_POSTCODE].isEmpty() || !validatePostcode(csvFields[CsvParser.INDEX_OF_POSTCODE])) {
			return addErrorAndMessage(messagePrefix + "has no postCode or its format is invalid.");
		}
		if(csvFields[CsvParser.INDEX_OF_SHIPPINGPRICE].isEmpty() || !validatePositiveDecimal(csvFields[CsvParser.INDEX_OF_SHIPPINGPRICE], 0.00f, true)) {
			return addErrorAndMessage(messagePrefix + "has no shippingPrice or its format is invalid.");
		}
		if(csvFields[CsvParser.INDEX_OF_SALEPRICE].isEmpty() || !validatePositiveDecimal(csvFields[CsvParser.INDEX_OF_SALEPRICE], 1.00f, false)) {
			return addErrorAndMessage(messagePrefix + "has no salePrice or its format is invalid.");
		}
		if(csvFields[CsvParser.INDEX_OF_SALEPRICE].isEmpty() || !validateStatus(csvFields[CsvParser.INDEX_OF_STATUS])) {
			return addErrorAndMessage(messagePrefix + "has no status or its format is invalid.");
		}
		responseOfValidation.setResponseFileStatus(ResponseFileStatus.OK);
		return responseOfValidation;
	}

	public boolean validateEmailFormat(String emailAddress) {
		final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
		final Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(emailAddress);
		boolean isValidEmail = matcher.matches();
		if(isValidEmail) {
			responseOfValidation.setBuyerEmail(emailAddress);
		}
		return isValidEmail;
	}

	public boolean validateDateFormat(String orderDate) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(sdf.parse(orderDate).getTime());
			if (!orderDate.equals(sdf.format(date))) {
				return false;
			}
			responseOfValidation.setOrderDate(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public boolean validatePostcode(String postCodeString) {
		try {
	        Integer postCodeInt = Integer.parseInt(postCodeString);
	        responseOfValidation.setPostCode(postCodeInt);
	        return true;
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	}
	
	public boolean validatePositiveDecimal(String numberString, float min, boolean isShippingPrice) {
		try {
			Float numberFloat = Float.parseFloat(numberString);
	        if(numberFloat >= min) {
	        	if(isShippingPrice) {
	        		responseOfValidation.setShippingPrice(numberFloat);
	        	} else {
	        		responseOfValidation.setSalePrice(numberFloat);
	        	}
	        	return true;
	        }
	        return false;
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	}
	
	
	
	public boolean validateStatus(String status) {
		try {
			OrderItemStatus orderItemStatus = OrderItemStatus.valueOf(status);
			responseOfValidation.setOrderItemStatus(orderItemStatus);
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
			responseOfValidation.setOrderId(id);
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
		boolean notFound = !rs.isBeforeFirst();
		
		if(notFound) {
			responseOfValidation.setOrderItemId(id);
		}
		
		return notFound;
	}
	
	public Long longParser(String orderId) {
		try {
			return Long.parseLong(orderId);
		} catch (NumberFormatException ex) {
	        return null;
	    }
	}
	
}
