package com.teklif.repository.config;

public class SheetNameResolver {

    public static String resolve(String urunKodu){

        if(urunKodu.startsWith("KASWRDIF"))
            return "KASWRDIF";

        if(urunKodu.startsWith("KANM"))
            return "KANM";

        if(urunKodu.startsWith("MNZ"))
            return "MNZ";

        if(urunKodu.startsWith("SLT"))
            return "SLT";

        if(urunKodu.startsWith("DMP"))
            return "DMP";

        throw new RuntimeException("Sheet resolve edilemedi: " + urunKodu);
    }
}