package com.teklif.pricing;

import java.util.*;

import com.teklif.model.OzellikTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.dto.PricingResult;
import com.teklif.repository.OzellikDeposu;
import com.teklif.model.OzellikOran;

public class PricingService {

    private SqlExcelFiyatService sqlService = new SqlExcelFiyatService();

    public PricingResult fiyatHesapla(
            PricingRequest req,
            Map<OzellikTipi,List<String>> secimler){

        double hamFiyat = sqlService.hamFiyatGetir(req);

        double oranEkleri = 0;
        double motorEk = 0;

        for(OzellikTipi tip : secimler.keySet()){

            Map<String, OzellikOran> oranMap =
                    OzellikDeposu.oranlariGetir(tip);

            if(oranMap == null) continue;

            for(String secim : secimler.get(tip)){

                OzellikOran oran = oranMap.get(secim);

                if(oran == null) continue;

                if(!oran.isSabit()){
                    oranEkleri += hamFiyat * oran.getOran();
                }
            }
        }

        List<String> aksesuarlar =
                secimler.get(OzellikTipi.AKSESUAR_TIPI);

        if(aksesuarlar != null){

            boolean motorVar =
                    aksesuarlar.contains("Servo Motor 24V") ||
                    aksesuarlar.contains("Servo Motor230V") ||
                    aksesuarlar.contains("Limit Switch");

            if(motorVar && req.getMotorFiyati()!=null){
                motorEk = req.getMotorFiyati() * 1.1;
            }
        }

        double toplam = hamFiyat + oranEkleri + motorEk;

        return new PricingResult(
                hamFiyat,
                oranEkleri,
                motorEk,
                toplam
        );
    }
}
