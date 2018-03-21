package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zsidodaniel.orderprocessor.dbconnector.DatabaseConnectionFactory;

/**
 * Dao for accessing and modifying data in the order_item table.
 * @author Zsidó Dániel
 *
 */

public class OrderItemDao {
	
	private static OrderItemDao instance = null;
	
	private static final DatabaseConnectionFactory databaseConnectionFactory = DatabaseConnectionFactory.getInstance(); 	
	private static final String TABLENAME = "order_item";
	
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
		String queryById = "select * from \"" + TABLENAME + "\" where order_item_id = ?";
		
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
	
	public Integer save(Long orderItemId, Long orderId, Float salePrice, Float shippingPrice, String sku, OrderItemStatus orderItemStatus) {
		String insertSQL = "insert into \"" + TABLENAME + "\" (order_item_id, order_id, sale_price, shipping_price,"
							+ "total_item_price, sku, status) values"
							+ "(?,?,?,?,?,?,?)";
		try(Connection conn = databaseConnectionFactory.getConnection()) {
			System.out.println("Trying to insert new orderItem into the database with id: " + orderItemId);
			PreparedStatement statement = conn.prepareStatement(insertSQL);
			statement.setLong(1, orderItemId);
			statement.setLong(2, orderId);
			statement.setFloat(3, salePrice);
			statement.setFloat(4, shippingPrice);
			statement.setFloat(5, shippingPrice + salePrice);
			statement.setString(6, sku);
			statement.setString(7, orderItemStatus.name());
			return statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Could not execute query " + insertSQL);
			e.printStackTrace();
			return null;
		}
	}

}
