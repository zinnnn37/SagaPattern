// PaymentServiceClient.java
package com.study.order.service;

import com.study.order.dto.PaymentRequest;
import com.study.order.dto.PaymentResponse;
import com.study.order.exception.PaymentDeclinedException;
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

	// @Retry, @CircuitBreaker 제거
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
}