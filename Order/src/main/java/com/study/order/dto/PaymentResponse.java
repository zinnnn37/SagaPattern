package com.study.order.dto;

import com.study.order.domain.PaymentStatus;
import lombok.Data;

@Data
public class PaymentResponse {
	private int paymentId;
	private int orderId;
	private PaymentStatus status;
}