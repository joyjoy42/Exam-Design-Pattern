package com.badwallet.api.repository;

import com.badwallet.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWalletPhoneNumberOrderByCreatedAtDesc(String walletPhoneNumber);
}
