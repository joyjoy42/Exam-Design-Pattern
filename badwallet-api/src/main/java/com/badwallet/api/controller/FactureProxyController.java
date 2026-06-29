package com.badwallet.api.controller;

import com.badwallet.api.dto.FactureDto;
import com.badwallet.api.service.proxy.FactureServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Thin pass-through over {@link FactureServiceProxy} — the Proxy pattern lives in the service layer. */
@RestController
@RequestMapping("/api/external/factures")
@RequiredArgsConstructor
public class FactureProxyController {

    private final FactureServiceProxy factureServiceProxy;

    @GetMapping("/{walletCode}/current")
    public List<FactureDto> current(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        return factureServiceProxy.getCurrentMonthFactures(walletCode, unite);
    }

    @GetMapping("/{walletCode}/periode")
    public List<FactureDto> periode(
            @PathVariable String walletCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return factureServiceProxy.getFacturesByPeriod(walletCode, debut, fin);
    }
}
