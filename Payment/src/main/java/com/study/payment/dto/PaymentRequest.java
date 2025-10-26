package com.study.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
	private int orderId;
	private int userId;
	private int amount;
}