// PaymentService.java
package com.study.payment.service;

import com.study.payment.domain.Payment;
import com.study.payment.domain.PaymentStatus;
import com.study.payment.dto.PaymentRequest;
import com.study.payment.dto.PaymentResponse;
import com.study.payment.exception.PaymentDeclinedException;
import com.study.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;

	@Value("${payment.test.error-type:none}")
	private String errorType;

	@Value("${payment.test.failure-rate:0}")
	private double failureRate;

	@Value("${payment.test.retry-count:0}")
	private int retryCount;

	private int currentRetry = 0;

	@Transactional
	public PaymentResponse processPayment(PaymentRequest request) {
		log.info("결제 요청: orderId={}, userId={}, amount={}",
				request.getOrderId(), request.getUserId(), request.getAmount());

		// 에러 시뮬레이션
		simulateError();

		// 정상 처리
		Payment payment = Payment.builder()
				.orderId(request.getOrderId())
				.userId(request.getUserId())
				.amount(request.getAmount())
				.status(PaymentStatus.COMPLETED)
				.build();

		payment = paymentRepository.save(payment);
		log.info("결제 완료: paymentId={}", payment.getId());

		return PaymentResponse.from(payment);
	}

	public PaymentResponse getPayment(int paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new IllegalArgumentException("결제 없음"));
		return PaymentResponse.from(payment);
	}

	private void simulateError() {
		if ("decline".equals(errorType)) {
			throw new PaymentDeclinedException("카드 한도 초과");
		}

		if ("server-error".equals(errorType)) {
			currentRetry++;
			log.warn("서버 에러 발생 ({}번째 시도)", currentRetry);

			// N번째 시도에서 성공
			if (retryCount > 0 && currentRetry >= retryCount) {
				log.info("{}번 시도 후 성공", currentRetry);
				currentRetry = 0;
				return;  // 정상 처리
			}

			// 에러 발생
			throw new RuntimeException("서버 내부 오류 (테스트)");
		}

		// Circuit Breaker 테스트
		if ("circuit-breaker".equals(errorType) && Math.random() < failureRate) {
			log.error("Circuit Breaker 테스트 - 에러 발생");
			throw new RuntimeException("서버 내부 오류 (Circuit Breaker 테스트)");
		}
	}
}