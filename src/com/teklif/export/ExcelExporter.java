package com.teklif.export;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

    // =====================================================
    // ⭐ HTML -> EXCEL TEXT (br -> \n, tag temizleme)
    // =====================================================
    private static String toExcelText(Object value){
        if(value == null) return "";

        String s = value.toString();

        // HTML kullanıyorsan (JTable için)
        if(s.toLowerCase().startsWith("<html>")){
            // <br> / <hr> -> yeni satır
            s = s.replaceAll("(?i)<br\\s*/?>", "\n");
            s = s.replaceAll("(?i)<hr\\s*/?>", "\n");

            // tüm html taglerini sil
            s = s.replaceAll("<[^>]*>", "");
        }

        // normalde de <br> gelebilir diye (garanti)
        s = s.replace("<br>", "\n").replace("<hr>", "\n");

        return s.trim();
    }

    public static void export(JTable table) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Excel Kaydet");
        chooser.setSelectedFile(new File("teklif.xlsx"));

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Teklif");

            // =====================================================
            // ⭐ NEF TEKNİK HEADER BLOĞU
            // =====================================================

            // FONTLAR
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short)10);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short)14);

            // STYLE
            CellStyle center = workbook.createCellStyle();
            center.setAlignment(HorizontalAlignment.CENTER);
            center.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle boldLeft = workbook.createCellStyle();
            boldLeft.setFont(boldFont);

            // =====================================================
            // SOL ÜST BLOK
            // =====================================================
            Row r0 = sheet.createRow(0);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0,2,0,4));

            Cell c00 = r0.createCell(0);
            c00.setCellValue("NEF TEKNİK HAVALANDIRMA İNŞAAT SAN. VE TİC.LTD.ŞTİ.");
            c00.setCellStyle(boldLeft);

            // =====================================================
            // ORTA LOGO ALANI
            // =====================================================
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0,2,5,9));

            Row r1 = sheet.createRow(1);
            Cell logo = r1.createCell(5);
            logo.setCellValue("N E F   T E K N İ K");
            logo.setCellStyle(center);

            // =====================================================
            // SAĞ ÜST BLOK
            // =====================================================
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0,2,10,13));

            Cell doc = r0.createCell(10);
            doc.setCellValue("DOKÜMAN : NEF-FORM.1");

            // =====================================================
            // TEKLİF FORMU BAŞLIK
            // =====================================================
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3,3,0,13));

            Row r3 = sheet.createRow(3);
            Cell teklif = r3.createCell(0);
            teklif.setCellValue("TEKLİF FORMU");
            teklif.setCellStyle(center);

            TableModel model = table.getModel();

            // =====================================
            // HEADER
            // =====================================

            // =====================================
            // ⭐ MULTI LINE HEADER (ERP STYLE)
            // =====================================

            // header font
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            // header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);

            int tableStartRow = 6;   // ⭐ tablo artık aşağıdan başlıyor
            Row headerRow = sheet.createRow(tableStartRow);

            // =====================================================
            // ⭐ GENEL TOPLAM / KDV / KDV DAHİL
            // =====================================================

            double genelToplam = 0;

            // TOPLAM FİYAT kolonu index = 13
            for(int r=0; r<model.getRowCount(); r++){

                Object val = model.getValueAt(r,13);

                if(val == null) continue;

                try{
                    String temiz = val.toString()
                            .replace("TL","")
                            .replace(",",".")
                            .trim();

                    genelToplam += Double.parseDouble(temiz);
                }catch(Exception ignore){}
            }

            double kdv = genelToplam * 0.20;
            double kdvDahil = genelToplam + kdv;

            // tablo bitiş satırı
            int summaryStart = tableStartRow + 1 + model.getRowCount() + 2;

            // sağa hizalamak için son kolon
            int lastCol = model.getColumnCount()-1;

            // GENEL TOPLAM
            Row sumRow = sheet.createRow(summaryStart);
            sumRow.createCell(lastCol-1).setCellValue("GENEL TOPLAM");
            sumRow.createCell(lastCol).setCellValue(genelToplam);

            // KDV
            Row kdvRow = sheet.createRow(summaryStart+1);
            kdvRow.createCell(lastCol-1).setCellValue("KDV %20");
            kdvRow.createCell(lastCol).setCellValue(kdv);

            // KDV DAHİL
            Row dahilRow = sheet.createRow(summaryStart+2);
            dahilRow.createCell(lastCol-1).setCellValue("KDV DAHİL GENEL");
            dahilRow.createCell(lastCol).setCellValue(kdvDahil);

            for(int i=0;i<model.getColumnCount();i++){

                Cell cell = headerRow.createCell(i);

                String header = model.getColumnName(i);

                // HTML temizleme
                header = header.replace("<html>", "")
                        .replace("</html>", "")
                        .replace("<hr>", "\n")
                        .replace("<br>", "\n");

                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }

            // header yüksekliği (3 satır için)
            headerRow.setHeightInPoints(55);

            // =====================================
            // DATA
            // =====================================

            // ⭐ wrap style (data için)
            CellStyle wrapDataStyle = workbook.createCellStyle();
            wrapDataStyle.setWrapText(true);
            wrapDataStyle.setVerticalAlignment(VerticalAlignment.TOP);

            for(int r=0;r<model.getRowCount();r++){

                Row row = sheet.createRow(tableStartRow + 1 + r);

                boolean rowHasNewLine = false;

                for(int c=0;c<model.getColumnCount();c++){

                    Object val = model.getValueAt(r,c);

                    Cell cell = row.createCell(c);

                    String text = toExcelText(val);

                    if(!text.isBlank()){
                        cell.setCellValue(text);
                    }

                    // yeni satır varsa wrap aç
                    if(text.contains("\n")){
                        cell.setCellStyle(wrapDataStyle);
                        rowHasNewLine = true;
                    }
                }

                // satırda yeni satır varsa yüksekliği arttır
                if(rowHasNewLine){
                    row.setHeightInPoints(35);
                }
            }

            // AUTO SIZE
            for(int i=0;i<model.getColumnCount();i++){
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
            }

            try(FileOutputStream fos = new FileOutputStream(file)){
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(null,"Excel başarıyla oluşturuldu.");

        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Excel hata: "+ex.getMessage());
        }
    }
}