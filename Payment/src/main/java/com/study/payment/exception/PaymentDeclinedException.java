package com.study.payment.exception;

public class PaymentDeclinedException extends RuntimeException {
	public PaymentDeclinedException(String message) {
		super(message);
	}
}
