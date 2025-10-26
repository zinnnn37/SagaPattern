package com.study.order.exception;

public class PaymentFailedException extends RuntimeException {
  public PaymentFailedException(String message, Throwable e) {
    super(message, e);
  }
}
