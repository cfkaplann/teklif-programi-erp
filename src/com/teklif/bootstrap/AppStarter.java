package com.teklif.bootstrap;

import javax.swing.SwingUtilities;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.teklif.db.ConnectionManager;
import com.teklif.db.SchemaInitializer;
import com.teklif.importer.MasterExcelImporter;
import com.teklif.ui.MainFrame;

public class AppStarter {

// ⭐ Programın çalıştığı klasör (Eclipse / MSI / EXE fark etmez)
	private static final String BASE = System.getProperty("user.dir");

	public static void main(String[] args) {

		try {

			System.out.println("🚀 AppStarter başladı...");

			ensureDatabase();
			ensurePriceData();

			System.out.println("🚀 UI açılıyor...");
			System.out.println(
				    AppStarter.class.getResource("/HAM_FIYATLAR.xlsx")
				);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		SwingUtilities.invokeLater(() -> {

			System.out.println("🚀 MainFrame oluşturuluyor...");
			new MainFrame().setVisible(true);
		});
	}

// ======================================================
// ⭐ DB VAR MI? YOKSA OLUŞTUR
// ======================================================
	private static void ensureDatabase() throws Exception {

		File dbFile = com.teklif.system.AppPathManager.getDatabaseFile();

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

		// ⭐ ConnectionManager kullanıyoruz
		try (Connection conn = ConnectionManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM price_table");
				ResultSet rs = ps.executeQuery()) {

			empty = rs.getInt(1) == 0;
		}

		System.out.println("📊 Excel fiyat tabloları kontrol ediliyor...");

		MasterExcelImporter importer = new MasterExcelImporter();
		String excelPath = extractExcelFromResource();
		importer.importAll(excelPath);

		System.out.println("✅ Excel import / güncelleme tamamlandı.");
	}



	private static String extractExcelFromResource() throws Exception {

		java.io.InputStream is = AppStarter.class.getResourceAsStream("/HAM_FIYATLAR.xlsx");

		if (is == null) {
			throw new RuntimeException("Excel resource bulunamadı!");
		}

		File temp = File.createTempFile("HAM_FIYATLAR", ".xlsx");

		temp.deleteOnExit();

		java.nio.file.Files.copy(is, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

		System.out.println("EXCEL TEMP = " + temp.getAbsolutePath());

		return temp.getAbsolutePath();
	}

}