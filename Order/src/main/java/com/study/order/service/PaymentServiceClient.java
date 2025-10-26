package com.study.order.service;

import com.study.order.dto.PaymentRequest;
import com.study.order.dto.PaymentResponse;
import com.study.order.exception.PaymentDeclinedException;
import com.study.order.exception.PaymentFailedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

	private final RestTemplate restTemplate;

	@Value("${payment-service.url}")
	private String paymentServiceUrl;

	@CircuitBreaker(name = "paymentService")
	@Retry(name = "paymentService", fallbackMethod = "paymentFallback")
	public PaymentResponse processPayment(PaymentRequest request) {
		log.info("결제 요청: orderId={}", request.getOrderId());

		try {
			ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
					paymentServiceUrl + "/api/payments",
					request,
					PaymentResponse.class
			);
			log.info("결제 성공: paymentId={}", response.getBody().getPaymentId());
			return response.getBody();

		} catch (HttpClientErrorException.BadRequest e) {
			log.warn("결제 거부");
			throw new PaymentDeclinedException("카드 한도 초과");
		}
	}

	private PaymentResponse paymentFallback(PaymentRequest request, Exception e) {
		log.error("결제 폴백 실행 - 모든 재시도 실패: orderId={}, error={}",
				request.getOrderId(), e.getMessage());
		throw new PaymentFailedException("결제 서비스 호출 실패", e);
	}
}