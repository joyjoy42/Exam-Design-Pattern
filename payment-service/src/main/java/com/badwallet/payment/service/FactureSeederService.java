package com.badwallet.payment.service;

import com.badwallet.payment.entity.Facture;
import com.badwallet.payment.entity.FactureStatus;
import com.badwallet.payment.entity.ServiceName;
import com.badwallet.payment.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates sample invoice history per wallet so the consultation/payment endpoints
 * have realistic data to act on. Wallet codes follow the same {@code WLT-0000001}
 * convention used by badwallet-api's own seeder, so the two services line up.
 */
@Service
@RequiredArgsConstructor
public class FactureSeederService {

    private static final int MONTHS_OF_HISTORY = 4;

    private final FactureRepository factureRepository;

    @Transactional
    public int seed(int numWallets, int monthsOfHistory) {
        int months = monthsOfHistory > 0 ? monthsOfHistory : MONTHS_OF_HISTORY;
        List<Facture> factures = new ArrayList<>();
        for (int walletIndex = 1; walletIndex <= numWallets; walletIndex++) {
            String walletCode = "WLT-%07d".formatted(walletIndex);
            for (ServiceName serviceName : ServiceName.values()) {
                factures.addAll(buildHistory(walletCode, walletIndex, serviceName, months));
            }
        }
        factureRepository.saveAll(factures);
        return factures.size();
    }

    private List<Facture> buildHistory(String walletCode, int walletIndex, ServiceName serviceName, int months) {
        List<Facture> history = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int index = 1; index <= months; index++) {
            boolean isCurrent = index == months;
            LocalDate period = currentMonth.minusMonths(months - index);
            BigDecimal amount = BigDecimal.valueOf(random.nextInt(15, 91) * 100L);

            FactureStatus status;
            BigDecimal amountDue;
            if (isCurrent) {
                status = FactureStatus.UNPAID;
                amountDue = amount;
            } else if (random.nextInt(100) < 70) {
                status = FactureStatus.PAID;
                amountDue = BigDecimal.ZERO;
            } else {
                status = FactureStatus.UNPAID;
                amountDue = amount;
            }

            history.add(Facture.builder()
                    .reference("FAC-%s-%d-%d".formatted(serviceName, walletIndex, index))
                    .walletCode(walletCode)
                    .serviceName(serviceName)
                    .amount(amount)
                    .amountDue(amountDue)
                    .status(status)
                    .periodMonth(period.getMonthValue())
                    .periodYear(period.getYear())
                    .issuedAt(period.withDayOfMonth(Math.min(28, random.nextInt(1, 6))))
                    .build());
        }
        return history;
    }
}
