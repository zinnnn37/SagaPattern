package com.study.order.exception;

public class PaymentDeclinedException extends RuntimeException {
	public PaymentDeclinedException(String message) {
		super(message);
	}
}
