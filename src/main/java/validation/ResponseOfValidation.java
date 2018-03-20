package validation;

import dao.ResponseFileStatus;

public class ResponseOfValidation {

	private String lineNumber;
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
	
}
