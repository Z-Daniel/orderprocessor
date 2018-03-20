package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zsidodaniel.orderprocesser.dbconnector.DatabaseConnectionFactory;

public class OrderDao {
	
	private static OrderDao instance = null;
	
	private static final DatabaseConnectionFactory databaseConnectionFactory = DatabaseConnectionFactory.getInstance(); 	
	
	public static OrderDao getInstance() {
		synchronized (OrderDao.class) {
			if (instance == null)
				instance = new OrderDao();
			return instance;
		}
	}
	
	private OrderDao() {
		
	}
	
	public ResultSet findById(Long id) {
		String queryById = "select * from \"order\" where order_id = ?";
		
		try(Connection conn = databaseConnectionFactory.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(queryById);
			statement.setLong(1, id);
			return statement.executeQuery();
		} catch (SQLException e) {
			System.out.println("Could not execute query " + queryById);
			e.printStackTrace();
			return null;
		}
	}
	//TODO lehet hogy a date-nek defaultban a mai napra kéne mutatnia?
	public Integer save(Long orderId, String buyerName, String buyerEmail, Date orderDate, Float orderTotalValue, String address, Integer postcode) { //TODO return saved object or boolean
		String insertSQL = "insert into \"order\" (order_id, buyer_name, buyer_email,"
							+ "order_date, order_total_value, address, postcode) values"
							+ "(?,?,?,?,?,?,?)";
		try(Connection conn = databaseConnectionFactory.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(insertSQL);
			statement.setLong(1, orderId);
			statement.setString(2, buyerName);
			statement.setString(3, buyerEmail);
			statement.setDate(4, orderDate);
			statement.setFloat(5, orderTotalValue);
			statement.setString(6, address);
			statement.setInt(7, postcode);
			return statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Could not execute query " + insertSQL);
			e.printStackTrace();
			return null;
		}
	}

	public Integer addToOrderTotalValue(Long orderId, Float value) {
		String updateSQL = "update order set order_total_value = order_total_value + ? where order_id = ?";
		try(Connection conn = databaseConnectionFactory.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(updateSQL);
			statement.setFloat(1, value);
			statement.setLong(2, orderId);
			return statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Could not execute query " + updateSQL);
			e.printStackTrace();
			return null;
		}
	}
	
}
