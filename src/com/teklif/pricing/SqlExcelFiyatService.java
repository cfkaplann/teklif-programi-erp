package com.teklif.pricing;

import com.teklif.model.OlcuTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.factory.StrategyFactory;
import com.teklif.pricing.strategy.PricingStrategy;
import com.teklif.repository.config.OlcuTipiResolver;

public class SqlExcelFiyatService {

    public double hamFiyatGetir(PricingRequest req){

        String sheet = req.getSheetName();

        // ⭐ Ölçü tipi çöz
        OlcuTipi tip = OlcuTipiResolver.resolve(sheet);

        // ⭐ Strategy al
        PricingStrategy strategy =
                StrategyFactory.getStrategy(tip);

        // ⭐ Execute
        return strategy.execute(req);
    }
}
