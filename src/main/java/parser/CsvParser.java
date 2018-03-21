package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import com.zsidodaniel.orderprocessor.dbconnector.DatabaseConnectionFactory;

import dao.OrderDao;
import dao.OrderItemDao;
import dao.ResponseFileStatus;
import validation.OrderValidator;
import validation.ResponseOfValidation;

/**
 * Parse data from the input file, commit results to a response file
 * and to the database.
 * @author Zsidó Dániel
 *
 */

public class CsvParser {

	private static CsvParser instance = null;
	
	private final OrderValidator orderValidator = OrderValidator.getInstance();
	
	private static final String PROPERTY_FILENAME = "orderprocessor.properties";
	private static final String USERNAME_PROP = "ftp.username";
	private static final String PASSWORD_PROP = "ftp.password";
	private static final String PATH_PROP = "ftp.path";
	private static final String HOST_PROP = "ftp.host";
	
	private static final String HEADER_OF_RESPONSE = "LineNumber;Status;Message";
	private static final String RESPONSE_CSV_NAME = "responseFile.txt";
	private static final String INPUT_CSV_NAME = "inputFile.txt";
	private static final String CSV_SPLITARATOR = ";";
	private static final boolean CSV_HAS_HEADER = true;
	
	public static final int CSV_NUMBER_OF_FIELDS = 12;

	public static final int INDEX_OF_LINENUMBER = 0;
	public static final int INDEX_OF_ORDERITEMID = 1;
	public static final int INDEX_OF_ORDERID = 2;
	public static final int INDEX_OF_BUYERNAME = 3;
	public static final int INDEX_OF_BUYEREMAIL = 4;
	public static final int INDEX_OF_ADDRESS = 5;
	public static final int INDEX_OF_POSTCODE = 6;
	public static final int INDEX_OF_SALEPRICE = 7;
	public static final int INDEX_OF_SHIPPINGPRICE = 8;
	public static final int INDEX_OF_SKU = 9;
	public static final int INDEX_OF_STATUS = 10;
	public static final int INDEX_OF_ORDERDATE = 11;
	
	public static CsvParser getInstance() {
		synchronized (CsvParser.class) {
			if (instance == null)
				instance = new CsvParser();
			return instance;
		}
	}

	private CsvParser() {

	}

	public void readCsvFile() {
		ClassLoader classLoader = DatabaseConnectionFactory.class.getClassLoader();
		initResponseFile(classLoader);
		File csvFile = new File(classLoader.getResource(INPUT_CSV_NAME).getFile());
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			if(CSV_HAS_HEADER) {	
				line = br.readLine();
			}
			OrderDao orderDao = OrderDao.getInstance();
			OrderItemDao orderItemDao = OrderItemDao.getInstance();
			String[] csvFields = new String[CSV_NUMBER_OF_FIELDS];
			System.out.println("Starting to read from csv file: " + INPUT_CSV_NAME);
 			while ((line = br.readLine()) != null) {
 				
				csvFields = line.split(CSV_SPLITARATOR);
				ResponseOfValidation responseOfValidation = orderValidator.validateLine(csvFields);
				if(responseOfValidation.getResponseFileStatus() == ResponseFileStatus.OK) {
					if(responseOfValidation.isOrderShouldBeInserted()) {
						if(orderDao.save(responseOfValidation.getOrderId(),
									  csvFields[INDEX_OF_BUYERNAME],
									  responseOfValidation.getBuyerEmail(),
									  responseOfValidation.getOrderDate(),
									  csvFields[INDEX_OF_ADDRESS],
									  responseOfValidation.getPostCode()) == null) {
							System.out.println("Failed to insert new order into the database.");
						} else {
							System.out.println("Order was successfully inserted into the database.");
						}
					}
					if(orderItemDao.save(responseOfValidation.getOrderItemId(),
									  responseOfValidation.getOrderId(),
									  responseOfValidation.getSalePrice(),
									  responseOfValidation.getShippingPrice(),
									  csvFields[INDEX_OF_SKU],
									  responseOfValidation.getOrderItemStatus()) == null) {
						System.out.println("Failed to insert new orderItem into the database.");
					} else {
						System.out.println("OrderItem was successfully inserted into the database.");
					}
					if(orderDao.addToOrderTotalValue(responseOfValidation.getOrderId(),
												  responseOfValidation.getSalePrice() + responseOfValidation.getShippingPrice()) == null) {
						System.out.println("Failed to update order.");
					} else {
						System.out.println("Update of order was successful.");
					}
				}
				writeResponseFile(responseOfValidation.getMessage(), responseOfValidation.getLineNumber(), responseOfValidation.getResponseFileStatus());
			}
 			sendFileThroughFTP();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void initResponseFile(ClassLoader classLoader) {
		File responseFile = new File("./" + RESPONSE_CSV_NAME);
		try (BufferedWriter responseWriter = new BufferedWriter(new FileWriter(responseFile, false))) {
			responseWriter.write(HEADER_OF_RESPONSE);
			responseWriter.newLine();
		} catch (IOException e) {
			System.out.println("Could not write to responseFile.");
			e.printStackTrace();
		}
	}
	
	private void writeResponseFile(String message, String lineNumber, ResponseFileStatus responseFileStatus) {
		if(responseFileStatus == null) {
			responseFileStatus = ResponseFileStatus.ERROR;
		}
		if(message == null) {
			message = "";
		}
		File responseFile = new File("./" + RESPONSE_CSV_NAME);
		try (BufferedWriter responseWriter = new BufferedWriter(new FileWriter(responseFile, true))) {
			responseWriter.write(lineNumber + ";" + responseFileStatus.name() + ";" + message);
			responseWriter.newLine();
		} catch (IOException e) {
			System.out.println("Could not write to responseFile.");
			e.printStackTrace();
		}
	}

	private void sendFileThroughFTP() {
		
		Properties properties = getProperties();
		
		if( properties == null) {
			return;
		}
		System.out.println("Initializing ftp connection");
		String ftpUrl = "ftp://%s:%s@%s/%s;type=i";
        String host = properties.getProperty(HOST_PROP, "localhost:21");
        String user = properties.getProperty(USERNAME_PROP, "orderprocessor");
        String pass = properties.getProperty(PASSWORD_PROP, "1234");
        String filePath = "./" + RESPONSE_CSV_NAME;
        String uploadPath = properties.getProperty(PATH_PROP, "/") + RESPONSE_CSV_NAME;
        
        final int BUFFER_SIZE = 4096;
 
        ftpUrl = String.format(ftpUrl, user, pass, host, uploadPath);
        System.out.println("Upload URL: " + ftpUrl);
 
        try {
        	URL url = new URL(ftpUrl);
            URLConnection conn = url.openConnection();
            OutputStream outputStream = conn.getOutputStream();
            FileInputStream inputStream = new FileInputStream(filePath);
 
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            inputStream.close();
            outputStream.close();
 
            System.out.println("Upload of responseFile was successful");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	private Properties getProperties() {
		ClassLoader classLoader = DatabaseConnectionFactory.class.getClassLoader();
		Properties properties = new Properties(System.getProperties());
		try (InputStream propertyStream = classLoader.getResourceAsStream(PROPERTY_FILENAME)) {

			properties.load(propertyStream);
			return properties;
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
			System.out.println("Unable to find " + PROPERTY_FILENAME + " file in resources!");
		} catch (IOException ioException) {
			System.out.println("Could not load " + PROPERTY_FILENAME + " file!");
			ioException.printStackTrace();
		}
		return null;
	}
	
}
