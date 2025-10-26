package com.study.order.dto;

import com.study.order.domain.Order;
import com.study.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
	private int orderId;
	private int userId;
	private int amount;
	private OrderStatus status;
	private int paymentId;

	public static OrderResponse from(Order order) {
		return OrderResponse.builder()
				.orderId(order.getId())
				.userId(order.getUserId())
				.amount(order.getAmount())
				.status(order.getStatus())
				.paymentId(order.getPaymentId())
				.build();
	}
}
