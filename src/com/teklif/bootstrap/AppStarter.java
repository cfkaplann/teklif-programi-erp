package com.teklif.bootstrap;

import javax.swing.SwingUtilities;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.teklif.db.SchemaInitializer;
import com.teklif.importer.MasterExcelImporter;
import com.teklif.ui.MainFrame;

public class AppStarter {

    private static final String DB_PATH = "data/teklif.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static void main(String[] args) {

        try {

            System.out.println("🚀 AppStarter başladı...");

            ensureDatabase();
            ensurePriceData();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // UI her zaman en son açılır
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // ======================================================
    // ⭐ DB VAR MI? YOKSA OLUŞTUR
    // ======================================================
    private static void ensureDatabase() throws Exception {

        File dbFile = new File(DB_PATH);

        if (!dbFile.exists()) {

            System.out.println("📦 DB bulunamadı → Schema oluşturuluyor...");
            SchemaInitializer.init();

        } else {

            System.out.println("✅ DB mevcut.");

        }
    }

    // ======================================================
    // ⭐ FİYAT TABLOLARI VAR MI? YOKSA IMPORT ET
    // ======================================================
    private static void ensurePriceData() throws Exception {

        boolean empty = false;

        // ⭐ SADECE KONTROL İÇİN CONNECTION AÇ
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM price_table");
             ResultSet rs = ps.executeQuery()) {

            empty = rs.getInt(1) == 0;
        } // ⭐ BURADA CONNECTION KAPANDI (ÇOK ÖNEMLİ)

        // ⭐ IMPORT ARTIK AYRI ÇALIŞIYOR
        if (empty) {

            System.out.println("📊 Fiyat tabloları boş → Excel import başlıyor...");

            MasterExcelImporter importer = new MasterExcelImporter();
            importer.importAll("HAM_FIYATLAR.xlsx");

            System.out.println("✅ Excel import tamamlandı.");

        } else {

            System.out.println("✅ Fiyat tabloları zaten mevcut.");
        }
    }

}
