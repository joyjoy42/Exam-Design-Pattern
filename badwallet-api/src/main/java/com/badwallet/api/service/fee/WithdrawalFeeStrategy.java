package com.badwallet.api.service.fee;

import java.math.BigDecimal;

/**
 * Strategy: computes the fee charged on top of a withdrawal amount. Kept separate from
 * {@code WalletService} so the fee formula (currently flat 1%, capped) can change — e.g.
 * fee-free tiers, promotions — without touching the withdrawal flow itself.
 */
public interface WithdrawalFeeStrategy {
    BigDecimal calculateFee(BigDecimal amount);
}
