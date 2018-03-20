package com.zsidodaniel.orderprocesser.dbconnector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionFactory {

	// name of the database property file found in the resources
	private static final String PROPERTY_FILENAME = "database-connection.properties";

	// property names in the database properties file
	private static final String URL_PROP = "jdbc.url";
	private static final String USERNAME_PROP = "jdbc.username";
	private static final String PASSWORD_PROP = "jdbc.password";
	private static final String DRIVER_PROP = "jdbc.driver";

	// property names for setting up the database connection
	private static final String USERNAME_CONN_PROP = "user";
	private static final String PASSWORD_CONN_PROP = "password";

	private static String dbURL = "jdbc:postgresql://localhost:5432/orderprocesser";
	private static String dbDriver = "org.postgresql.Driver";
	private static Properties connProperties = new Properties();

	private static DatabaseConnectionFactory instance = null;

	private static boolean isInitialized = false;
	
	private DatabaseConnectionFactory() {
	}

	public static DatabaseConnectionFactory getInstance() {
		synchronized (DatabaseConnectionFactory.class) {
			if (instance == null)
				instance = new DatabaseConnectionFactory();

			return instance;
		}
	}

	public Connection getConnection() {
		if(!isInitialized) {
			boolean isSuccessful = readDbProperties();
			isInitialized = isSuccessful;
			if(isSuccessful) {				
				return createConnection();
			}
			return null;
		} else {
			return createConnection();
		}

	}

	private Connection createConnection() {
		try {
			return DriverManager.getConnection(dbURL, connProperties);
		} catch (SQLException sqlException) {
			System.out.println("Could not connect to database on URL: " + dbURL);
			sqlException.printStackTrace();
			return null;
		}
	}

	private boolean readDbProperties() {
		ClassLoader classLoader = DatabaseConnectionFactory.class.getClassLoader();
		Properties prop = new Properties(System.getProperties());
		try (InputStream propertyStream = classLoader.getResourceAsStream(PROPERTY_FILENAME)) {

			prop.load(propertyStream);

			connProperties.put(USERNAME_CONN_PROP, prop.getProperty(USERNAME_PROP));
			connProperties.put(PASSWORD_CONN_PROP, prop.getProperty(PASSWORD_PROP));

			dbURL = prop.getProperty(URL_PROP);
			dbDriver = prop.getProperty(DRIVER_PROP);

			Class.forName(dbDriver);
			return true;
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
			System.out.println("Unable to find " + PROPERTY_FILENAME + " file in resources!");
			return false;
		} catch (IOException ioException) {
			System.out.println("Could not load " + PROPERTY_FILENAME + " file!");
			ioException.printStackTrace();
			return false;
		} catch (ClassNotFoundException classNotFoundException) {
			System.out.println("Could not load " + dbDriver + ".");
			classNotFoundException.printStackTrace();
			return false;
		}
	}

}
