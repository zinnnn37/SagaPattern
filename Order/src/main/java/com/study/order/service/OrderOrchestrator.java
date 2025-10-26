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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrator {

	private final OrderRepository orderRepository;
	private final PaymentServiceClient paymentClient;

	@Transactional
	public OrderResponse createOrder(OrderRequest request) {
		// 1. 주문 생성
		Order order = Order.builder()
				.userId(request.getUserId())
				.amount(request.getAmount())
				.status(OrderStatus.PENDING)
				.build();
		order = orderRepository.save(order);
		log.info("주문 생성: orderId={}", order.getId());

		try {
			// 2. 결제 호출
			PaymentResponse payment = paymentClient.processPayment(
					PaymentRequest.from(order)
			);

			// 3. 주문 완료
			order.complete(payment.getPaymentId());
			orderRepository.save(order);
			log.info("주문 완료: orderId={}", order.getId());

			return OrderResponse.from(order);

		} catch (PaymentDeclinedException e) {
			// 결제 거부 - 즉시 보상
			log.warn("결제 거부 - 주문 취소: orderId={}", order.getId());
			compensateOrder(order);
			throw e;

		} catch (Exception e) {
			// 시스템 에러 - 보상
			log.error("결제 실패 - 주문 취소: orderId={}", order.getId());
			compensateOrder(order);
			throw e;
		}
	}

	private void compensateOrder(Order order) {
		order.cancel();
		orderRepository.save(order);
		log.info("보상 완료: orderId={} 취소됨", order.getId());
	}

	public OrderResponse getOrder(int orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		return OrderResponse.from(order);
	}
}