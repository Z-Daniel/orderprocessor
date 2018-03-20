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
 * Hello world!
 * AdminAki4kar ftp jelszó
 */
public class App {

    public static void main( String[] args ) {
    	
    		OrderDao orderDao = OrderDao.getInstance();
			CsvParser csvParser = CsvParser.getInstance();
			csvParser.readCsvFile();
//			ResultSet rs = orderDao.findById(2);
//			try {
//				if(!rs.isBeforeFirst()) { //nullra futhat
//					System.out.println("Empty");
//				}
//				while(rs.next()) {
//					System.out.println(rs.getString("buyer_name"));
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    }
    
}
