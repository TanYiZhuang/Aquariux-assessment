package com.aquariux.technical.assessment.trade.controller;

import com.aquariux.technical.assessment.trade.dto.request.TradeRequest;
import com.aquariux.technical.assessment.trade.dto.response.TradeResponse;
import com.aquariux.technical.assessment.trade.mapper.TradeMapper;
import com.aquariux.technical.assessment.trade.mapper.UserWalletMapper;
import com.aquariux.technical.assessment.trade.service.TradeServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade", description = "Trading operations")
@RequiredArgsConstructor
public class TradeController {

    private TradeServiceInterface tradeService;
    private final TradeMapper tradeMapper;
    // Add additional beans here if needed for your implementation

    @PostMapping(value = "/execute", produces = "application/json")
    @Operation(summary = "Execute trade", description = "Execute a buy or sell trade for cryptocurrency pairs")
    public ResponseEntity<TradeResponse> executeTrade(@RequestBody TradeRequest tradeRequest) {
        if(this.authenticate(tradeRequest)) {
        	return ResponseEntity.ok(tradeService.executeTrade(tradeRequest));
        }
        else {
        	return ResponseEntity.ok(this.createErrorResponse());
        }
    }

	private TradeResponse createErrorResponse() {
		TradeResponse response = new TradeResponse();
		response.setResultCode(1);
		response.setErrorMsg("Incorrect Username/Password/Email");
		return response;
	}

	private boolean authenticate(TradeRequest tradeRequest) {
		
		if (!CollectionUtils.isEmpty(tradeMapper.findByUserPwEmail(tradeRequest.getUsername(), tradeRequest.getPassword(), tradeRequest.getEmail()))) {
			return true;
		}
		return false;
	}
}