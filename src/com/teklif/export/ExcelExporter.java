package com.teklif.export;

import com.teklif.model.ParaBirimi;
import com.teklif.pricing.KurService;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

	private static final String TEMPLATE_PATH = "/teklif_sablon.xlsx";
	private static final String SHEET_NAME = "Teklif";
	private static final int TEMPLATE_ROW_INDEX = 25;

	private static final int[] COL_MAP = { 1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };

	public static void export(JTable table, ParaBirimi paraBirimi) {

		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new File("teklif.xlsx"));

		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;

		File file = chooser.getSelectedFile();

		try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
				Workbook workbook = new XSSFWorkbook(is);
				FileOutputStream fos = new FileOutputStream(file)) {

			Sheet sheet = workbook.getSheet(SHEET_NAME);
			TableModel model = table.getModel();
			int productCount = model.getRowCount();

			if (productCount == 0)
				return;

			// 🔥 Para formatı oluştur
			String symbol = paraBirimi.getSymbol();
			DataFormat df = workbook.createDataFormat();
			CellStyle currencyStyle = workbook.createCellStyle();
			currencyStyle.setDataFormat(df.getFormat("#,##0.00 \"" + symbol + "\""));

			for (int r = 0; r < productCount; r++) {

				Row row = sheet.getRow(TEMPLATE_ROW_INDEX + r);
				if (row == null)
					continue;

				clearMappedCells(row);

				for (int jCol = 0; jCol < COL_MAP.length; jCol++) {

					int xCol = COL_MAP[jCol];
					Cell cell = row.getCell(xCol);
					if (cell == null)
						cell = row.createCell(xCol);

					Object value = model.getValueAt(r, jCol);
					if (value == null)
						continue;

					// ----------------------------
					// 🔥 SIRA NO numeric
					// ----------------------------
					if (jCol == 0) {
						cell.setCellValue(Double.parseDouble(value.toString()));
						continue;
					}

					// ----------------------------
					// 🔥 Ölçüler & miktar numeric
					// ----------------------------
					if (jCol == 3 || jCol == 4 || jCol == 5 || jCol == 6 || jCol == 11) {

						try {

							double number = Double.parseDouble(value.toString());

							// 🔥 Eğer 0 ise hücreyi boş bırak
							if (number == 0) {
								cell.setBlank();
							} else {
								cell.setCellValue(number);
							}

						} catch (Exception ex) {
							cell.setBlank(); // ❗ artık 0 yazmıyoruz
						}

						continue;
					}
					// ----------------------------
					// 🔥 Fiyat kolonları
					// ----------------------------
					if (jCol == 13 || jCol == 14) {

						try {
							double tlValue = Double.parseDouble(value.toString());

							// ⭐ TL -> Seçili para birimi dönüşümü
							double converted = KurService.cevir(tlValue, paraBirimi);

							cell.setCellValue(converted);

							// Mevcut style'ı kopyala
							CellStyle originalStyle = cell.getCellStyle();
							CellStyle newStyle = workbook.createCellStyle();

							newStyle.cloneStyleFrom(originalStyle);
							newStyle.setDataFormat(
									workbook.createDataFormat().getFormat("#,##0.00 \"" + symbol + "\""));

							cell.setCellStyle(newStyle);

						} catch (Exception ex) {
							cell.setCellValue(0);
						}
						continue;
					}

					// ----------------------------
					// 🔥 Text alanlar
					// ----------------------------
					String text = value.toString();

					text = text.replaceAll("(?i)<br\\s*/?>", " ").replaceAll("(?i)</html>", "")
							.replaceAll("(?i)<html>", "").replaceAll("(?i)<[^>]+>", "");

					cell.setCellValue(text.trim());
				}
			}
			// -----------------------------
			// SUMMARY
			// -----------------------------

			int firstExcelRow = TEMPLATE_ROW_INDEX + 1;
			int lastExcelRow = TEMPLATE_ROW_INDEX + productCount;

			Row genelRow = sheet.getRow(126);
			Row kdvRow = sheet.getRow(127);
			Row dahilRow = sheet.getRow(128);

			if (genelRow != null) {

				Cell c = genelRow.getCell(18);
				if (c != null) {

					c.setCellFormula("SUM(S" + firstExcelRow + ":S" + lastExcelRow + ")");

					CellStyle original = c.getCellStyle();
					CellStyle newStyle = workbook.createCellStyle();
					newStyle.cloneStyleFrom(original);

					newStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"" + symbol + "\""));

					c.setCellStyle(newStyle);
				}
			}

			if (kdvRow != null) {
				Cell c = kdvRow.getCell(18);
				if (c != null) {

					c.setCellFormula("S127*0.20");

					CellStyle original = c.getCellStyle();
					CellStyle newStyle = workbook.createCellStyle();
					newStyle.cloneStyleFrom(original);

					newStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"" + symbol + "\""));

					c.setCellStyle(newStyle);
				}
			}

			if (dahilRow != null) {
				Cell c = dahilRow.getCell(18);
				if (c != null) {

					c.setCellFormula("S127+S128");

					CellStyle original = c.getCellStyle();
					CellStyle newStyle = workbook.createCellStyle();
					newStyle.cloneStyleFrom(original);

					newStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"" + symbol + "\""));

					c.setCellStyle(newStyle);
				}
			}
			
			PrintSetup printSetup = sheet.getPrintSetup();

			printSetup.setLandscape(false);
			printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);

			sheet.setFitToPage(true);
			printSetup.setFitWidth((short) 1);
			printSetup.setFitHeight((short) 0);

			int lastRowForPrint = TEMPLATE_ROW_INDEX + productCount + 5;

			workbook.setPrintArea(
			        workbook.getSheetIndex(sheet),
			        0,
			        18,
			        0,
			        lastRowForPrint
			);
			
			workbook.write(fos);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void clearMappedCells(Row row) {

		for (int xCol : COL_MAP) {

			// Ürün adı D:G merge alanı
			// Sadece D kolonunu temizle (3 index)
			if (xCol == 4 || xCol == 5 || xCol == 6)
				continue;

			Cell cell = row.getCell(xCol);

			if (cell != null) {
				cell.setBlank(); // setCellValue("") yerine bunu kullan
			}
		}
	}
}