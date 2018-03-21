package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.HashMap;
import java.util.stream.Stream;

import com.zsidodaniel.orderprocesser.dbconnector.DatabaseConnectionFactory;

import dao.OrderDao;
import dao.OrderItemDao;
import dao.ResponseFileStatus;
import validation.OrderValidator;
import validation.ResponseOfValidation;

public class CsvParser {

	private static CsvParser instance = null;
	
	private final OrderValidator orderValidator = OrderValidator.getInstance();
	
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
			int i = 0;
			if(CSV_HAS_HEADER) {
				line = br.readLine();
				i++;
			}
			OrderDao orderDao = OrderDao.getInstance();
			OrderItemDao orderItemDao = OrderItemDao.getInstance();
			String[] csvFields = new String[CSV_NUMBER_OF_FIELDS];
 			while ((line = br.readLine()) != null) {

				csvFields = line.split(CSV_SPLITARATOR);
				ResponseOfValidation responseOfValidation = orderValidator.validateLine(csvFields, i);
				if(responseOfValidation.getResponseFileStatus() == ResponseFileStatus.OK) {
					if(responseOfValidation.isOrderShouldBeInserted()) {
						orderDao.save(responseOfValidation.getOrderId(),
									  csvFields[INDEX_OF_BUYERNAME],
									  responseOfValidation.getBuyerEmail(),
									  responseOfValidation.getOrderDate(),
									  csvFields[INDEX_OF_ADDRESS],
									  responseOfValidation.getPostCode());
					}
					orderItemDao.save(responseOfValidation.getOrderItemId(),
									  responseOfValidation.getOrderId(),
									  responseOfValidation.getSalePrice(),
									  responseOfValidation.getShippingPrice(),
									  csvFields[INDEX_OF_SKU],
									  responseOfValidation.getOrderItemStatus());
					orderDao.addToOrderTotalValue(responseOfValidation.getOrderId(),
												  responseOfValidation.getSalePrice() + responseOfValidation.getShippingPrice());
				}
				writeResponseFile(responseOfValidation.getMessage(), responseOfValidation.getLineNumber(), responseOfValidation.getResponseFileStatus());
				i++;
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
		String ftpUrl = "ftp://localhost:14147";
        String host = "localhost";
        String user = "orderprocessor";
        String pass = "1234";
        String filePath = "./responseFile.txt";
        String uploadPath = "/";
        
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
 
            System.out.println("File uploaded");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
}
