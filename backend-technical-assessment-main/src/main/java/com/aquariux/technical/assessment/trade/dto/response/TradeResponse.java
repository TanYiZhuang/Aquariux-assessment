package com.aquariux.technical.assessment.trade.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TradeResponse {
	@JsonProperty("Result Code")
    private int resultCode;
	@JsonProperty("Remaining Balance")
    private BigDecimal remainingBalance;
	@JsonProperty("Message")
    private String errorMsg;
         
}