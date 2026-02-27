package com.teklif.export;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

    private static final String TEMPLATE_PATH = "/teklif_sablon.xlsx";
    private static final String SHEET_NAME = "Teklif";

    private static final int TEMPLATE_DATA_ROW_INDEX = 25; // Excel 26. satır

    // JTable kolon index -> Excel kolon index
    private static final int[] COL_MAP = {
            1, 2, 3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    };

    private static String toExcelText(Object value) {
        if (value == null) return "";
        String s = value.toString();

        if (s.toLowerCase().startsWith("<html>")) {
            s = s.replaceAll("(?i)<br\\s*/?>", "\n");
            s = s.replaceAll("(?i)<hr\\s*/?>", "\n");
            s = s.replaceAll("<[^>]*>", "");
        }

        return s.replace("<br>", "\n").replace("<hr>", "\n").trim();
    }

    public static void export(JTable table, com.teklif.model.ParaBirimi pb) {

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("teklif.xlsx"));

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();

        InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            JOptionPane.showMessageDialog(null, "Şablon bulunamadı!");
            return;
        }

        TableModel model = table.getModel();

        try (Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                JOptionPane.showMessageDialog(null, "Sheet bulunamadı!");
                return;
            }

            Row templateRow = sheet.getRow(TEMPLATE_DATA_ROW_INDEX);
            if (templateRow == null) {
                JOptionPane.showMessageDialog(null, "Template veri satırı yok!");
                return;
            }

            DataFormat df = workbook.createDataFormat();

            // ==========================
            // PARA STYLE (T ve U ayrı)
            // ==========================
            CellStyle templateT = templateRow.getCell(19).getCellStyle();
            CellStyle templateU = templateRow.getCell(20).getCellStyle();

            CellStyle moneyStyleT = workbook.createCellStyle();
            moneyStyleT.cloneStyleFrom(templateT);
            moneyStyleT.setDataFormat(df.getFormat(getExcelFormat(pb)));

            CellStyle moneyStyleU = workbook.createCellStyle();
            moneyStyleU.cloneStyleFrom(templateU);
            moneyStyleU.setDataFormat(df.getFormat(getExcelFormat(pb)));

            // ==========================
            // WRAP STYLE (ürün adı için)
            // ==========================
            CellStyle templateText = templateRow.getCell(3).getCellStyle();

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.cloneStyleFrom(templateText);
            wrapStyle.setWrapText(true);

            int dataStartRow = TEMPLATE_DATA_ROW_INDEX;

            // ==========================
            // DATA SATIRLARI
            // ==========================
            for (int r = 0; r < model.getRowCount(); r++) {

                int excelRowIndex = dataStartRow + r;

                Row row = sheet.getRow(excelRowIndex);
                if (row == null) row = sheet.createRow(excelRowIndex);

                copyRowStyle(templateRow, row, 0, 21);

                for (int c = 0; c < model.getColumnCount(); c++) {

                    int targetCol = COL_MAP[c];
                    Cell cell = row.getCell(targetCol);
                    if (cell == null) cell = row.createCell(targetCol);

                    Object val = model.getValueAt(r, c);
                    boolean written = false;

                    if (val instanceof Number) {

                        double d = ((Number) val).doubleValue();

                        if (c == 13 || c == 14) {

                            d = com.teklif.pricing.KurService.cevir(d, pb);

                            if (c == 14) { // Toplam Fiyat T:U
                                cell.setCellStyle(moneyStyleT);

                                Cell uCell = row.getCell(20);
                                if (uCell == null) uCell = row.createCell(20);
                                uCell.setCellStyle(moneyStyleU);
                            } else {
                                cell.setCellStyle(moneyStyleT);
                            }
                        }

                        cell.setCellValue(d);
                        written = true;
                    }

                    if (!written && val != null) {

                        String raw = toExcelText(val).replace(",", ".").trim();

                        try {
                            double d = Double.parseDouble(raw);

                            if (c == 13 || c == 14) {

                                d = com.teklif.pricing.KurService.cevir(d, pb);

                                if (c == 14) {
                                    cell.setCellStyle(moneyStyleT);

                                    Cell uCell = row.getCell(20);
                                    if (uCell == null) uCell = row.createCell(20);
                                    uCell.setCellStyle(moneyStyleU);
                                } else {
                                    cell.setCellStyle(moneyStyleT);
                                }
                            }

                            cell.setCellValue(d);
                            written = true;

                        } catch (Exception ignore) {}
                    }

                    if (!written) {

                        String text = toExcelText(val);
                        if (!text.isBlank()) cell.setCellValue(text);

                        if (text.contains("\n")) {
                            cell.setCellStyle(wrapStyle);
                        }
                    }
                }
            }

            // ==========================
            // SUMMARY
            // ==========================
            double genelTL = 0;
            for (int r = 0; r < model.getRowCount(); r++) {
                Object v = model.getValueAt(r, 14);
                if (v instanceof Number)
                    genelTL += ((Number) v).doubleValue();
            }

            double kdvTL = genelTL * 0.20;
            double dahilTL = genelTL + kdvTL;

            writeSummary(sheet, 44, 19,
                    com.teklif.pricing.KurService.cevir(genelTL, pb),
                    moneyStyleT, moneyStyleU);

            writeSummary(sheet, 45, 19,
                    com.teklif.pricing.KurService.cevir(kdvTL, pb),
                    moneyStyleT, moneyStyleU);

            writeSummary(sheet, 46, 19,
                    com.teklif.pricing.KurService.cevir(dahilTL, pb),
                    moneyStyleT, moneyStyleU);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(null, "Excel başarıyla oluşturuldu.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Excel hata: " + ex.getMessage());
        }
    }

    private static void copyRowStyle(Row src, Row dest, int from, int to) {
        dest.setHeight(src.getHeight());
        for (int c = from; c < to; c++) {
            Cell s = src.getCell(c);
            Cell d = dest.getCell(c);
            if (d == null) d = dest.createCell(c);
            if (s != null) d.setCellStyle(s.getCellStyle());
        }
    }

    private static void writeSummary(Sheet sheet, int rowIndex, int colIndex,
                                     double value,
                                     CellStyle styleT,
                                     CellStyle styleU) {

        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        Cell tCell = row.getCell(colIndex);
        if (tCell == null) tCell = row.createCell(colIndex);
        tCell.setCellValue(value);
        tCell.setCellStyle(styleT);

        Cell uCell = row.getCell(colIndex + 1);
        if (uCell == null) uCell = row.createCell(colIndex + 1);
        uCell.setCellStyle(styleU);
    }

    private static String getExcelFormat(com.teklif.model.ParaBirimi pb) {
        if (pb == null) return "#,##0.00 \"TL\"";

        switch (pb) {
            case EUR: return "#,##0.00 \"EUR\"";
            case USD: return "#,##0.00 \"USD\"";
            default:  return "#,##0.00 \"TL\"";
        }
    }
}