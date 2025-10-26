// OrderOrchestrator.java
package com.study.order.service;

import com.study.order.domain.Order;
import com.study.order.domain.OrderStatus;
import com.study.order.dto.OrderRequest;
import com.study.order.dto.OrderResponse;
import com.study.order.dto.PaymentRequest;
import com.study.order.dto.PaymentResponse;
import com.study.order.exception.PaymentDeclinedException;
import com.study.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrator {

	private final OrderRepository orderRepository;
	private final PaymentServiceClient paymentClient;

	public OrderResponse createOrder(OrderRequest request) {
		// 1. 주문 생성 (독립 트랜잭션)
		Order order = createOrderInNewTransaction(request);

		try {
			// 2. 결제 호출
			PaymentResponse payment = paymentClient.processPayment(
					PaymentRequest.from(order)
			);

			// 3. 주문 완료 (독립 트랜잭션)
			completeOrderInNewTransaction(order.getId(), payment.getPaymentId());

			return OrderResponse.from(
					orderRepository.findById(order.getId())
							.orElseThrow(() -> new IllegalArgumentException("주문 없음"))
			);

		} catch (PaymentDeclinedException e) {
			log.warn("결제 거부 - 보상 트랜잭션 실행: orderId={}", order.getId());
			cancelOrderInNewTransaction(order.getId());
			throw e;

		} catch (Exception e) {
			log.error("결제 실패 - 보상 트랜잭션 실행: orderId={}", order.getId());
			cancelOrderInNewTransaction(order.getId());
			throw e;
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Order createOrderInNewTransaction(OrderRequest request) {
		Order order = Order.builder()
				.userId(request.getUserId())
				.amount(request.getAmount())
				.status(OrderStatus.PENDING)
				.build();
		order = orderRepository.save(order);
		log.info("주문 생성: orderId={}", order.getId());
		return order;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void completeOrderInNewTransaction(int orderId, int paymentId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		order.complete(paymentId);
		orderRepository.save(order);
		log.info("주문 완료: orderId={}", order.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cancelOrderInNewTransaction(int orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		order.cancel();
		orderRepository.save(order);
		log.info("보상 트랜잭션 완료: orderId={} 취소됨", order.getId());
	}

	public OrderResponse getOrder(int orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		return OrderResponse.from(order);
	}
}