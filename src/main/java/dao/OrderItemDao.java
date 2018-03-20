package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zsidodaniel.orderprocesser.dbconnector.DatabaseConnectionFactory;

public class OrderItemDao {

private static OrderItemDao instance = null;
	
	private static final DatabaseConnectionFactory databaseConnectionFactory = DatabaseConnectionFactory.getInstance(); 	
	
	public static OrderItemDao getInstance() {
		synchronized (OrderItemDao.class) {
			if (instance == null)
				instance = new OrderItemDao();
			return instance;
		}
	}
	
	private OrderItemDao() {
		
	}
	
	public ResultSet findById(Long id) {
		String queryById = "select * from \"order_item\" where order_id = ?";
		
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

}
