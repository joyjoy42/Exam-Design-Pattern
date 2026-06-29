package com.badwallet.api.controller;

import com.badwallet.api.dto.BalanceResponse;
import com.badwallet.api.dto.CreateWalletRequest;
import com.badwallet.api.dto.DepositRequest;
import com.badwallet.api.dto.WalletResponse;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletResponse.from(wallet));
    }

    @GetMapping
    public Page<WalletResponse> list(Pageable pageable) {
        return walletService.list(pageable).map(WalletResponse::from);
    }

    @GetMapping("/{phoneNumber}")
    public WalletResponse getByPhoneNumber(@PathVariable String phoneNumber) {
        return WalletResponse.from(walletService.getByPhoneNumber(phoneNumber));
    }

    @GetMapping("/{phoneNumber}/balance")
    public BalanceResponse getBalance(@PathVariable String phoneNumber) {
        return BalanceResponse.from(walletService.getByPhoneNumber(phoneNumber));
    }

    @PostMapping("/{id}/deposit")
    public WalletResponse deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest request) {
        return WalletResponse.from(walletService.deposit(id, request));
    }
}
