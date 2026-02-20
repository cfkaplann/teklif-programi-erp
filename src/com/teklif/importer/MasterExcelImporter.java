package com.teklif.importer;

import java.io.File;
import java.sql.Connection;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.teklif.db.ConnectionManager;

public class MasterExcelImporter {

    public void importAll(String excelPath) {

        try(Connection conn = ConnectionManager.getConnection()){

            conn.createStatement().execute("PRAGMA busy_timeout = 5000");
            conn.setAutoCommit(false);

            try {

                Workbook wb = WorkbookFactory.create(new File(excelPath));

                AutoExcelImporter auto = new AutoExcelImporter();
                auto.importAll(conn, wb);

                conn.commit(); // ✅ sadece başarılıysa

                System.out.println("✅ Excel import tamamlandı.");

            } catch(Exception e) {

                conn.rollback(); // ⭐ ÇOK ÖNEMLİ
                throw e;
            }

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
