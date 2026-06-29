package com.badwallet.payment.repository;

import com.badwallet.payment.entity.Facture;
import com.badwallet.payment.entity.ServiceName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByWalletCodeOrderByPeriodYearAscPeriodMonthAsc(String walletCode);

    List<Facture> findByWalletCodeAndServiceNameOrderByPeriodYearAscPeriodMonthAsc(
            String walletCode, ServiceName serviceName);

    List<Facture> findByWalletCodeAndIssuedAtBetweenOrderByIssuedAtAsc(
            String walletCode, LocalDate start, LocalDate end);

    List<Facture> findByReferenceIn(List<String> references);

    boolean existsByWalletCode(String walletCode);
}
