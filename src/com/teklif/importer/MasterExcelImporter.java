package com.teklif.importer;

import java.io.File;
import java.sql.Connection;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.teklif.db.ConnectionManager;

public class MasterExcelImporter {

    public void importAll(String excelPath) {

        File file = new File(excelPath);

        // ⭐⭐⭐ PROFESYONEL KONTROL
        if(!file.exists()){
            JOptionPane.showMessageDialog(
                null,
                "Excel dosyası bulunamadı!\n\n"
                + "Lütfen şu klasöre koyunuz:\n"
                + file.getAbsolutePath(),
                "Excel Bulunamadı",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try(Connection conn = ConnectionManager.getConnection()){

            conn.createStatement().execute("PRAGMA busy_timeout = 5000");
            conn.setAutoCommit(false);

            try {

                Workbook wb = WorkbookFactory.create(file);

                AutoExcelImporter auto = new AutoExcelImporter();
                auto.importAll(conn, wb);

                conn.commit();

                System.out.println("✅ Excel import tamamlandı.");

            } catch(Exception e) {

                conn.rollback();
                throw e;
            }

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}