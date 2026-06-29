package com.badwallet.api.controller;

import com.badwallet.api.dto.BalanceResponse;
import com.badwallet.api.dto.CreateWalletRequest;
import com.badwallet.api.dto.DepositRequest;
import com.badwallet.api.dto.PayFacturesRequest;
import com.badwallet.api.dto.PayRequest;
import com.badwallet.api.dto.TransactionResponse;
import com.badwallet.api.dto.TransferRequest;
import com.badwallet.api.dto.TransferResponse;
import com.badwallet.api.dto.WalletResponse;
import com.badwallet.api.dto.WithdrawRequest;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.service.WalletSeederService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletSeederService walletSeederService;

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed(
            @RequestParam(defaultValue = "10") int numWallets,
            @RequestParam(defaultValue = "100") int eventsPerWallet) {
        walletSeederService.seed(numWallets, eventsPerWallet);
        return ResponseEntity.accepted().body(Map.of(
                "status", "STARTED",
                "numWallets", numWallets,
                "eventsPerWallet", eventsPerWallet));
    }

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

    @PostMapping("/withdraw")
    public WalletResponse withdraw(@Valid @RequestBody WithdrawRequest request) {
        return WalletResponse.from(walletService.withdraw(request));
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request);
    }

    @GetMapping("/{phoneNumber}/transactions")
    public List<TransactionResponse> getTransactionHistory(@PathVariable String phoneNumber) {
        return walletService.getTransactionHistory(phoneNumber).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping("/pay")
    public WalletResponse pay(@Valid @RequestBody PayRequest request) {
        return WalletResponse.from(walletService.pay(request));
    }

    @PostMapping("/pay-factures")
    public WalletResponse payFactures(@Valid @RequestBody PayFacturesRequest request) {
        return WalletResponse.from(walletService.payFactures(request));
    }
}
