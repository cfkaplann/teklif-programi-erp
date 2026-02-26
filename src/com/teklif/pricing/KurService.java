package com.teklif.pricing;

import com.teklif.model.ParaBirimi;

public class KurService {

    // Şimdilik sabit kur (sonra API bağlarız)
    public static double euroKur() {
        return 52.0;
    }

    public static double dolarKur() {
        return 44.0;
    }

    public static double cevir(double tl, ParaBirimi pb) {

        switch(pb){
            case EUR:
                return tl / euroKur();

            case USD:
                return tl / dolarKur();

            default:
                return tl;
        }
    }
}