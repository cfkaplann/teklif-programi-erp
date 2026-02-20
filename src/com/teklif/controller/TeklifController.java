package com.teklif.controller;

import java.util.List;
import java.util.Map;

import com.teklif.model.OzellikTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.PricingService;
import com.teklif.pricing.dto.PricingResult;

public class TeklifController {

    private PricingService pricingService = new PricingService();

    // =====================================================
    // ⭐ CANLI FİYAT
    // =====================================================

    public PricingResult hesapla(PricingRequest request,
                                 Map<OzellikTipi, List<String>> secimler){

        return pricingService.fiyatHesapla(request, secimler);
    }

}
