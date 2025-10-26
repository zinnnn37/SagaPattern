package com.study.payment.controller;

import com.study.payment.dto.PaymentRequest;
import com.study.payment.dto.PaymentResponse;
import com.study.payment.exception.PaymentDeclinedException;
import com.study.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
		try {
			PaymentResponse response = paymentService.processPayment(request);
			return ResponseEntity.ok(response);
		} catch (PaymentDeclinedException e) {
			log.warn("결제 거부: orderId={}", request.getOrderId());
			return ResponseEntity.badRequest().body("결제 거부: " + e.getMessage());
		} catch (Exception e) {
			log.error("결제 처리 중 오류: orderId={}", request.getOrderId(), e);
			return ResponseEntity.internalServerError().body("결제 실패: " + e.getMessage());
		}
	}

	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentResponse> getPayment(@PathVariable int paymentId) {
		return ResponseEntity.ok(paymentService.getPayment(paymentId));
	}
}