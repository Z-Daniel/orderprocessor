package com.zsidodaniel.orderprocesser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zsidodaniel.orderprocesser.dbconnector.DatabaseConnectionFactory;

import dao.OrderDao;
import parser.CsvParser;

/**
 * AdminAki4kar ftp jelszó
 */
public class App {

    public static void main( String[] args ) {
			CsvParser csvParser = CsvParser.getInstance();
			csvParser.readCsvFile();
    }
    
}
