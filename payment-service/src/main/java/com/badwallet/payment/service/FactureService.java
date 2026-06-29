package com.badwallet.payment.service;

import com.badwallet.payment.dto.PaymentResult;
import com.badwallet.payment.entity.Facture;
import com.badwallet.payment.entity.FactureStatus;
import com.badwallet.payment.entity.ServiceName;
import com.badwallet.payment.exception.FactureNotFoundException;
import com.badwallet.payment.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;

    public List<Facture> getCurrentMonth(String walletCode, ServiceName serviceName) {
        LocalDate today = LocalDate.now();
        return factureRepository.findByWalletCodeOrderByPeriodYearAscPeriodMonthAsc(walletCode).stream()
                .filter(f -> f.isCurrentMonth(today) && f.isUnpaid())
                .filter(f -> serviceName == null || f.getServiceName() == serviceName)
                .toList();
    }

    public List<Facture> getByPeriod(String walletCode, LocalDate debut, LocalDate fin) {
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("'debut' must be before 'fin'");
        }
        return factureRepository
                .findByWalletCodeAndIssuedAtBetweenOrderByIssuedAtAsc(walletCode, debut, fin)
                .stream()
                .filter(Facture::isUnpaid)
                .toList();
    }

    public List<Facture> getByReferences(List<String> references) {
        List<Facture> found = factureRepository.findByReferenceIn(references);
        if (found.size() != references.size()) {
            List<String> missing = new ArrayList<>(references);
            found.forEach(f -> missing.remove(f.getReference()));
            throw new FactureNotFoundException("Factures introuvables: " + missing);
        }
        return found;
    }

    /**
     * Applies {@code amount} FIFO against the current month's unpaid factures for the
     * given wallet/service. Any portion of {@code amount} left over once those factures
     * are fully settled is not carried forward (only the current month can be paid here).
     */
    @Transactional
    public PaymentResult payCurrentMonth(String walletCode, ServiceName serviceName, BigDecimal amount) {
        List<Facture> due = getCurrentMonth(walletCode, serviceName).stream()
                .sorted(Comparator.comparing(Facture::getIssuedAt))
                .toList();
        if (due.isEmpty()) {
            throw new FactureNotFoundException(
                    "Aucune facture impayée du mois en cours pour " + walletCode + "/" + serviceName);
        }
        return applyPaymentFifo(due, amount);
    }

    @Transactional
    public PaymentResult payByReferences(List<String> references) {
        List<Facture> factures = getByReferences(references);
        BigDecimal totalApplied = BigDecimal.ZERO;
        List<String> paid = new ArrayList<>();
        for (Facture facture : factures) {
            totalApplied = totalApplied.add(facture.getAmountDue());
            facture.setAmountDue(BigDecimal.ZERO);
            facture.setStatus(FactureStatus.PAID);
            paid.add(facture.getReference());
        }
        factureRepository.saveAll(factures);
        return new PaymentResult(totalApplied, BigDecimal.ZERO, paid, List.of());
    }

    private PaymentResult applyPaymentFifo(List<Facture> due, BigDecimal amount) {
        BigDecimal remainingPayment = amount;
        BigDecimal applied = BigDecimal.ZERO;
        List<String> paid = new ArrayList<>();
        List<String> partiallyPaid = new ArrayList<>();

        for (Facture facture : due) {
            if (remainingPayment.signum() <= 0) {
                break;
            }
            BigDecimal toApply = remainingPayment.min(facture.getAmountDue());
            facture.setAmountDue(facture.getAmountDue().subtract(toApply));
            applied = applied.add(toApply);
            remainingPayment = remainingPayment.subtract(toApply);

            if (facture.getAmountDue().signum() == 0) {
                facture.setStatus(FactureStatus.PAID);
                paid.add(facture.getReference());
            } else {
                facture.setStatus(FactureStatus.PARTIALLY_PAID);
                partiallyPaid.add(facture.getReference());
            }
        }
        factureRepository.saveAll(due);

        BigDecimal remainingDue = due.stream().map(Facture::getAmountDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PaymentResult(applied, remainingDue, paid, partiallyPaid);
    }
}
