package com.study.order.dto;

import lombok.Data;

@Data
public class OrderRequest {
	private int userId;
	private int amount;
}
