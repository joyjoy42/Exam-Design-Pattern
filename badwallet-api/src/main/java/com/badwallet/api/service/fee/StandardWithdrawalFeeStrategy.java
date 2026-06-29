package com.badwallet.api.service.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 1% of the withdrawal amount, capped at 5000 XOF. */
@Component
public class StandardWithdrawalFeeStrategy implements WithdrawalFeeStrategy {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.01");
    private static final BigDecimal FEE_CAP = BigDecimal.valueOf(5000);

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return fee.min(FEE_CAP);
    }
}
