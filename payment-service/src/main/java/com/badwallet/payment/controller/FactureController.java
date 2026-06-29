package com.badwallet.payment.controller;

import com.badwallet.payment.dto.FactureResponse;
import com.badwallet.payment.dto.PayByReferencesRequest;
import com.badwallet.payment.dto.PayCurrentMonthRequest;
import com.badwallet.payment.dto.PaymentResult;
import com.badwallet.payment.entity.ServiceName;
import com.badwallet.payment.service.FactureSeederService;
import com.badwallet.payment.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;
    private final FactureSeederService factureSeederService;

    @GetMapping("/{walletCode}/current")
    public List<FactureResponse> current(
            @PathVariable String walletCode,
            @RequestParam(required = false) ServiceName unite) {
        return factureService.getCurrentMonth(walletCode, unite).stream().map(FactureResponse::from).toList();
    }

    @GetMapping("/{walletCode}/periode")
    public List<FactureResponse> periode(
            @PathVariable String walletCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return factureService.getByPeriod(walletCode, debut, fin).stream().map(FactureResponse::from).toList();
    }

    @GetMapping("/by-references")
    public List<FactureResponse> byReferences(@RequestParam List<String> refs) {
        return factureService.getByReferences(refs).stream().map(FactureResponse::from).toList();
    }

    @PostMapping("/pay")
    public PaymentResult pay(@Valid @RequestBody PayCurrentMonthRequest request) {
        return factureService.payCurrentMonth(request.walletCode(), request.serviceName(), request.amount());
    }

    @PostMapping("/pay-by-references")
    public PaymentResult payByReferences(@Valid @RequestBody PayByReferencesRequest request) {
        return factureService.payByReferences(request.factureReferences());
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed(
            @RequestParam(defaultValue = "10") int numWallets,
            @RequestParam(defaultValue = "4") int monthsOfHistory) {
        int created = factureSeederService.seed(numWallets, monthsOfHistory);
        return ResponseEntity.ok(Map.of(
                "numWallets", numWallets,
                "monthsOfHistory", monthsOfHistory,
                "facturesCreated", created));
    }
}
