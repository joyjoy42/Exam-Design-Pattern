package com.badwallet.api.service;

import com.badwallet.api.dto.DepositRequest;
import com.badwallet.api.dto.PayRequest;
import com.badwallet.api.dto.TransferRequest;
import com.badwallet.api.dto.WithdrawRequest;
import com.badwallet.api.entity.PaymentMethod;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.repository.WalletRepository;
import com.badwallet.api.service.proxy.FactureServiceProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Backs {@code POST /api/wallets/seed}. Runs off the request thread (see
 * {@code @EnableAsync} on the main class) because generating
 * {@code numWallets * eventsPerWallet} transactions can take a while. Each random event
 * reuses the same WalletService methods the public endpoints call, so seeded data goes
 * through the exact same Strategy/Template-Method/Observer/Proxy machinery as a real
 * request would.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSeederService {

    private static final String[] SERVICE_NAMES = {"ISM", "WOYAFAL"};
    private static final int FACTURE_HISTORY_MONTHS = 4;

    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final FactureServiceProxy factureServiceProxy;

    @Async
    public void seed(int numWallets, int eventsPerWallet) {
        seedPaymentServiceFactures(numWallets);
        List<Wallet> wallets = seedWallets(numWallets);

        int succeeded = 0;
        for (Wallet wallet : wallets) {
            for (int i = 0; i < eventsPerWallet; i++) {
                if (runRandomEvent(wallet, wallets)) {
                    succeeded++;
                }
            }
        }
        log.info("Seeding finished: {} wallets, {}/{} events applied",
                wallets.size(), succeeded, (long) wallets.size() * eventsPerWallet);
    }

    private void seedPaymentServiceFactures(int numWallets) {
        try {
            factureServiceProxy.seedFactures(numWallets, FACTURE_HISTORY_MONTHS);
        } catch (RuntimeException ex) {
            log.warn("payment-service seeding skipped (service unreachable?): {}", ex.getMessage());
        }
    }

    private List<Wallet> seedWallets(int numWallets) {
        List<Wallet> wallets = new ArrayList<>();
        for (int i = 1; i <= numWallets; i++) {
            String phoneNumber = "+22177%07d".formatted(i);
            Wallet existing = walletRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (existing != null) {
                wallets.add(existing);
                continue;
            }
            wallets.add(walletRepository.save(Wallet.builder()
                    .code("WLT-%07d".formatted(i))
                    .phoneNumber(phoneNumber)
                    .email("wallet%d@seed.local".formatted(i))
                    .balance(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(50_000, 500_000)))
                    .currency("XOF")
                    .createdAt(Instant.now())
                    .build()));
        }
        return wallets;
    }

    /** @return true if the randomly chosen event was applied, false if skipped (e.g. insufficient balance). */
    private boolean runRandomEvent(Wallet wallet, List<Wallet> allWallets) {
        try {
            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0 -> walletService.deposit(wallet.getId(), randomDeposit());
                case 1 -> walletService.withdraw(new WithdrawRequest(wallet.getPhoneNumber(), randomSmallAmount()));
                case 2 -> walletService.transfer(randomTransfer(wallet, allWallets));
                default -> walletService.pay(randomPay(wallet));
            }
            return true;
        } catch (RuntimeException ex) {
            log.debug("Skipped seed event for {}: {}", wallet.getPhoneNumber(), ex.getMessage());
            return false;
        }
    }

    private DepositRequest randomDeposit() {
        PaymentMethod method = ThreadLocalRandom.current().nextBoolean()
                ? PaymentMethod.CREDIT_CARD
                : PaymentMethod.WALLET_TARGET;
        return new DepositRequest(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(1_000, 20_000)), method);
    }

    private TransferRequest randomTransfer(Wallet sender, List<Wallet> allWallets) {
        if (allWallets.size() < 2) {
            throw new IllegalStateException("Pas assez de portefeuilles pour simuler un transfert");
        }
        Wallet receiver;
        do {
            receiver = allWallets.get(ThreadLocalRandom.current().nextInt(allWallets.size()));
        } while (receiver.getPhoneNumber().equals(sender.getPhoneNumber()));
        return new TransferRequest(sender.getPhoneNumber(), receiver.getPhoneNumber(), randomSmallAmount());
    }

    private PayRequest randomPay(Wallet wallet) {
        String serviceName = SERVICE_NAMES[ThreadLocalRandom.current().nextInt(SERVICE_NAMES.length)];
        return new PayRequest(wallet.getPhoneNumber(), serviceName,
                BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(500, 3_000)));
    }

    private BigDecimal randomSmallAmount() {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(500, 5_000));
    }
}
