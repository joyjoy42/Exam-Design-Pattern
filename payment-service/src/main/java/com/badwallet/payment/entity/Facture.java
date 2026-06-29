package com.badwallet.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An invoice ("facture") issued by an external utility (ISM, WOYAFAL) against a wallet.
 * Built exclusively through {@link Facture#builder()} (Builder pattern) so seeding code
 * can assemble instances without telescoping constructors.
 */
@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String walletCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceName serviceName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FactureStatus status;

    @Column(nullable = false)
    private int periodMonth;

    @Column(nullable = false)
    private int periodYear;

    @Column(nullable = false)
    private LocalDate issuedAt;

    public boolean isCurrentMonth(LocalDate today) {
        return periodMonth == today.getMonthValue() && periodYear == today.getYear();
    }

    public boolean isUnpaid() {
        return status != FactureStatus.PAID;
    }
}
