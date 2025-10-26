package com.study.order.service;

import com.study.order.domain.Order;
import com.study.order.dto.OrderRequest;
import com.study.order.dto.OrderResponse;
import com.study.order.dto.PaymentRequest;
import com.study.order.dto.PaymentResponse;
import com.study.order.exception.PaymentDeclinedException;
import com.study.order.exception.PaymentFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrator {

	private final OrderTransactionService transactionService;
	private final PaymentServiceClient paymentClient;

	public OrderResponse createOrder(OrderRequest request) {
		// 1. 주문 생성
		Order order = transactionService.createOrder(request);

		try {
			// 2. 결제 호출
			PaymentResponse payment = paymentClient.processPayment(
					PaymentRequest.from(order)
			);

			// 3. 주문 완료
			transactionService.completeOrder(order.getId(), payment.getPaymentId());

			return OrderResponse.from(
					transactionService.getOrder(order.getId())
			);

		} catch (PaymentDeclinedException e) {
			log.warn("결제 거부 - 보상 트랜잭션 실행: orderId={}", order.getId());
			transactionService.cancelOrder(order.getId());
			throw e;
		} catch (PaymentFailedException e) {
			log.error("결제 실패 (Resilience4j) - 보상 트랜잭션 실행: orderId={}", order.getId());
			transactionService.cancelOrder(order.getId());
			throw e;

		} catch (Exception e) {
			log.error("결제 실패 - 보상 트랜잭션 실행: orderId={}", order.getId());
			transactionService.cancelOrder(order.getId());
			throw e;
		}
	}

	public OrderResponse getOrder(int orderId) {
		Order order = transactionService.getOrder(orderId);
		return OrderResponse.from(order);
	}
}