package com.zsidodaniel.orderprocesser;

import parser.CsvParser;

/**
 * Csvparsing application for inserting valid data into the database.
 * @author Zsidó Dániel
 */
public class App {

    public static void main( String[] args ) {
			CsvParser csvParser = CsvParser.getInstance();
			csvParser.readCsvFile();
			System.exit(0);
    }
    
}
