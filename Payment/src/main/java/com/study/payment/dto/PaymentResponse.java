package com.study.payment.dto;

import com.study.payment.domain.Payment;
import com.study.payment.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentResponse {
	private int paymentId;
	private int orderId;
	private PaymentStatus status;

	public static PaymentResponse from(Payment payment) {
		return PaymentResponse.builder()
				.paymentId(payment.getId())
				.orderId(payment.getOrderId())
				.status(payment.getStatus())
				.build();
	}
}