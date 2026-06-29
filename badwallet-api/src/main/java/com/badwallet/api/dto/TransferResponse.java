package com.badwallet.api.dto;

import com.badwallet.api.entity.Wallet;

public record TransferResponse(WalletResponse sender, WalletResponse receiver) {
    public static TransferResponse of(Wallet sender, Wallet receiver) {
        return new TransferResponse(WalletResponse.from(sender), WalletResponse.from(receiver));
    }
}
