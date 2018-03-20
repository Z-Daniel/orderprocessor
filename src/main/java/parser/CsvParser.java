package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.HashMap;
import java.util.stream.Stream;

import com.zsidodaniel.orderprocesser.dbconnector.DatabaseConnectionFactory;

import dao.OrderDao;
import validation.OrderValidator;
import validation.ResponseOfValidation;

public class CsvParser {

	private static CsvParser instance = null;
	
	private final OrderValidator orderValidator = OrderValidator.getInstance(); 
	
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

		File csvFile = new File(classLoader.getResource(INPUT_CSV_NAME).getFile());
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			int i = 0;
			if(CSV_HAS_HEADER) {
				line = br.readLine();
				i++;
			}
			OrderDao orderDao = OrderDao.getInstance();
			while ((line = br.readLine()) != null) {

				String[] csvFields = line.split(CSV_SPLITARATOR);
				ResponseOfValidation responseOfValidation = orderValidator.validateLine(csvFields, i);
				if(responseOfValidation.isOrderShouldBeInserted()) {
//					Date orderDate = csvFields[INDEX_OF_ORDERDATE].isEmpty() ? new Date() : 
//					orderDao.save(csvFields[INDEX_OF_ORDERID],
//								  csvFields[INDEX_OF_BUYERNAME],
//								  csvFields[INDEX_OF_BUYEREMAIL],
//								  csvFields[],
//								  orderTotalValue,
//								  address,
//								  postcode); TODO mi lesz a parse-olással?
				} else {
					
				}
				
//				for (int i = 0; i < csvFields.length; i++) {
//					System.out.println(csvFields[i]);
//				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
