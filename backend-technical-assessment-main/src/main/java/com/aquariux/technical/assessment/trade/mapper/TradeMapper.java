package com.aquariux.technical.assessment.trade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import com.aquariux.technical.assessment.trade.entity.Trade;
import com.aquariux.technical.assessment.trade.entity.User;
import com.aquariux.technical.assessment.trade.entity.UserWallet;

@Mapper
public interface TradeMapper {
    
    // TODO: What database operations do you need for trading?
	 @Insert("""
	            INSERT INTO trades (user_id, crypto_pair_id, trade_type, quantity, price, total_amount, trade_time)
	            VALUES (#{userId}, #{cryptoPairId}, #{tradeType}, #{quantity}, #{price}, #{totalAmount}, #{tradeTime})
	            """)
	void insertNewTxn(Trade trade);
	 
	 @Select("""
	            SELECT FROM users WHERE username = #{userName} and password = #{passWord} and email = #{email}
	            """)
		List<User> findByUserPwEmail(String userName, String passWord, String email);
    // Feel free to add multiple methods, complex queries, or additional mapper interfaces as needed
	 
	  @Insert("""
	            INSERT INTO user_wallets (user_id, symbol_id, balance, updated_at) 
	            VALUES (#{cryptoPairId}, #{bidPrice}, #{askPrice}, #{bidSource}, #{askSource})
	            """)
	    void insertNewWallet(UserWallet userWallet);
	  
	  @Update("""
	            UPDATE user_wallets set balance = #{}, updated_at = #{} where user_id = #{} and symbol_id = {})
	            """)
	    void updateWallet(UserWallet userWallet);
}