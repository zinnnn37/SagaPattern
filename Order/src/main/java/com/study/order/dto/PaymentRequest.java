package com.study.order.dto;

import com.study.order.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
	private int orderId;
	private int userId;
	private int amount;

	public static PaymentRequest from(Order order) {
		return PaymentRequest.builder()
				.orderId(order.getId())
				.userId(order.getUserId())
				.amount(order.getAmount())
				.build();
	}
}