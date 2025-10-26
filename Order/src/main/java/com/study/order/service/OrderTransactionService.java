package com.study.order.service;

import com.study.order.domain.Order;
import com.study.order.domain.OrderStatus;
import com.study.order.dto.OrderRequest;
import com.study.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTransactionService {

	private final OrderRepository orderRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Order createOrder(OrderRequest request) {
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
	public void completeOrder(int orderId, int paymentId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		order.complete(paymentId);
		orderRepository.save(order);
		log.info("주문 완료: orderId={}", order.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cancelOrder(int orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
		order.cancel();
		orderRepository.save(order);
		log.info("보상 트랜잭션 완료: orderId={} 취소됨", order.getId());
	}

	@Transactional(readOnly = true)
	public Order getOrder(int orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문 없음"));
	}
}