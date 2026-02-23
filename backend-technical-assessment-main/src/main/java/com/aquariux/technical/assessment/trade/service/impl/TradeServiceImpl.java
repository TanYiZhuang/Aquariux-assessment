package com.aquariux.technical.assessment.trade.service.impl;


import com.aquariux.technical.assessment.trade.dto.request.TradeRequest;
import com.aquariux.technical.assessment.trade.dto.response.BestPriceResponse;
import com.aquariux.technical.assessment.trade.dto.response.TradeResponse;
import com.aquariux.technical.assessment.trade.dto.response.WalletBalanceResponse;
import com.aquariux.technical.assessment.trade.entity.Trade;
import com.aquariux.technical.assessment.trade.entity.UserWallet;
import com.aquariux.technical.assessment.trade.enums.TradeType;
import com.aquariux.technical.assessment.trade.mapper.CryptoPairMapper;
import com.aquariux.technical.assessment.trade.mapper.TradeMapper;
import com.aquariux.technical.assessment.trade.mapper.UserWalletMapper;
import com.aquariux.technical.assessment.trade.service.PriceServiceInterface;
import com.aquariux.technical.assessment.trade.service.TradeServiceInterface;
import com.aquariux.technical.assessment.trade.service.WalletServiceInterface;

import feign.Response;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeServiceInterface {

    private TradeMapper tradeMapper;
    private WalletServiceInterface userWallet;
    private PriceServiceInterface priceService;
    private CryptoPairMapper pairMapper;

    @Override
    public TradeResponse executeTrade(TradeRequest tradeRequest) {
        // TODO: Implement the core trading engine
    	TradeResponse response = new TradeResponse();
    	List<WalletBalanceResponse> userWalletList = userWallet.getUserWalletBalances(tradeRequest.getUserId());
    	WalletBalanceResponse userWallet = (WalletBalanceResponse) userWalletList.stream().filter(x -> x.getSymbol().equals(tradeRequest.getAssetType()));
    	List<BestPriceResponse> priceList = priceService.getLatestBestPrices();
		Long pairId = pairMapper.findIdByPairName(userWallet.getSymbol()+ tradeRequest.getTargetType());
	    if(pairId == null) pairId = pairMapper.findIdByPairName(tradeRequest.getTargetType() +userWallet.getSymbol());
	    if(pairId == null) {
    		response.setErrorMsg("No such pairing of crypto found");
    		return response;    		
    	}
	    
    	if(StringUtils.isNotBlank(response.getErrorMsg())){
    		
    		if(TradeType.BUY.equals(tradeRequest.getTradeType())) {
    			response = this.executeBuy(userWallet, tradeRequest, priceList, pairId, response);
    		}
    		else if(TradeType.SELL.equals(tradeRequest.getTradeType())) {
    			response =  this.executeSell(userWallet, tradeRequest, priceList, pairId, response);
    		}
    		else {
    			response = this.createUnsupportedErrorResponse(response);
    		}
    	}
		return response;
    	
    	
    }
    private TradeResponse executeBuy(WalletBalanceResponse userWallet, TradeRequest tradeRequest, List<BestPriceResponse> priceList, Long pairId, TradeResponse response) {
		BestPriceResponse bestPrice = Collections.min(priceList, Comparator.comparing(p -> p.getAskPrice()));
    	BigDecimal totalPrice = bestPrice.getAskPrice().multiply(tradeRequest.getAmountToUse());
    	BigDecimal remainingBalance = userWallet.getBalance().subtract(totalPrice);
    	if(remainingBalance.compareTo(new BigDecimal(0)) < 0) {    		
        		response.setErrorMsg("User Wallet does not have enough balance");
        		response.setResultCode(0);
        		return response;
    	}
    	this.insertNewTradeTxn(bestPrice.getAskPrice(), tradeRequest, pairId, totalPrice);
    	this.updateWallet(tradeRequest, remainingBalance);
    	return this.createSuccessResponse(response, remainingBalance);
    	
    }

	private void updateWallet(TradeRequest tradeRequest, BigDecimal remainingBalance) {
		UserWallet userWallet = new UserWallet();
		userWallet.setBalance(remainingBalance);
		userWallet.setId(tradeRequest.getUserId());
		userWallet.setUpdatedAt(LocalDateTime.now());
		//insert update to both bought/sold symbol_id
	}
	
	private TradeResponse createSuccessResponse(TradeResponse response, BigDecimal remainingBalance) {
		response.setErrorMsg("Txn Completed Successfully");
		response.setRemainingBalance(remainingBalance);
		response.setResultCode(0);
		return response;
	}
	private void insertNewTradeTxn(BigDecimal bestPrice, TradeRequest tradeRequest, Long pairId, BigDecimal totalPrice) {
		Trade tradeTxn = new Trade();
    	tradeTxn.setCryptoPairId(pairId);
    	tradeTxn.setPrice(bestPrice);
    	tradeTxn.setQuantity(tradeRequest.getAmountToUse());
    	tradeTxn.setTotalAmount(totalPrice);
    	tradeTxn.setTradeTime(LocalDateTime.now());
    	tradeTxn.setTradeType(tradeRequest.getTradeType().toString());
    	tradeTxn.setUserId(tradeRequest.getUserId());
		tradeMapper.insertNewTxn(tradeTxn);
	}
	private TradeResponse executeSell(WalletBalanceResponse userWallet, TradeRequest tradeRequest, List<BestPriceResponse> priceList, Long pairId, TradeResponse response) {
		BestPriceResponse bestPrice = Collections.min(priceList, Comparator.comparing(p -> p.getBidPrice()));
    	BigDecimal totalPrice = bestPrice.getBidPrice().multiply(tradeRequest.getAmountToUse());
    	BigDecimal remainingBalance = userWallet.getBalance().add(totalPrice);
    	if(remainingBalance.compareTo(new BigDecimal(0)) < 0) {    		
        		response.setErrorMsg("User Wallet does not have enough balance");
        		response.setResultCode(0);
        		return response;
    	}
    	this.insertNewTradeTxn(bestPrice.getBidPrice(), tradeRequest, pairId, totalPrice);
    	this.updateWallet(tradeRequest, remainingBalance);
    	return this.createSuccessResponse(response, remainingBalance);
	}

	
	private TradeResponse createUnsupportedErrorResponse(TradeResponse response) {
		response.setErrorMsg("Unsupport Function");
		response.setResultCode(1);;
		return null;
	}
}