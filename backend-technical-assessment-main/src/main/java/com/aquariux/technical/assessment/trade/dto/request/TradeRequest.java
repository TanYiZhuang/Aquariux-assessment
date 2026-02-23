package com.aquariux.technical.assessment.trade.dto.request;

import java.math.BigDecimal;

import com.aquariux.technical.assessment.trade.enums.TradeType;
import lombok.Data;

@Data
public class TradeRequest {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private TradeType tradeType;
    private String assetType;
    private String targetType;
    private BigDecimal amountToUse;
    // TODO: What information do you need to execute a trade?
}