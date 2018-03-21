package validation;

import java.sql.Date;

import dao.OrderItemStatus;
import dao.ResponseFileStatus;

/**
 * Contains the result of a linevalidation.
 * @author Zsidó Dániel
 *
 */

public class ResponseOfValidation {

	private String lineNumber;
	private Long orderId;
	private Long orderItemId;
	private String buyerEmail;
	private Date orderDate;
	private Integer postCode;
	private Float shippingPrice;
	private Float salePrice;
	private OrderItemStatus orderItemStatus; 
	
	private ResponseFileStatus responseFileStatus;
	private String message;
	private boolean orderShouldBeInserted = true;
	
	public ResponseOfValidation(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public ResponseFileStatus getResponseFileStatus() {
		return responseFileStatus;
	}
	
	public void setResponseFileStatus(ResponseFileStatus responseFileStatus) {
		this.responseFileStatus = responseFileStatus;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isOrderShouldBeInserted() {
		return orderShouldBeInserted;
	}
	
	public void setOrderShouldBeInserted(boolean orderShouldBeInserted) {
		this.orderShouldBeInserted = orderShouldBeInserted;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	public String getBuyerEmail() {
		return buyerEmail;
	}

	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Integer getPostCode() {
		return postCode;
	}

	public void setPostCode(Integer postCode) {
		this.postCode = postCode;
	}

	public Float getShippingPrice() {
		return shippingPrice;
	}

	public void setShippingPrice(Float shippingPrice) {
		this.shippingPrice = shippingPrice;
	}

	public Float getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Float salePrice) {
		this.salePrice = salePrice;
	}

	public OrderItemStatus getOrderItemStatus() {
		return orderItemStatus;
	}

	public void setOrderItemStatus(OrderItemStatus orderItemStatus) {
		this.orderItemStatus = orderItemStatus;
	}
	
	
}
