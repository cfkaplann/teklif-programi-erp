package com.teklif.ui.table;

import javax.swing.*;
import com.teklif.export.ExcelExporter;
import com.teklif.model.ParaBirimi;
import com.teklif.pricing.KurService;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TeklifTablePanel extends JPanel {

	private JTable table;
	private DefaultTableModel model;
	private ParaBirimi paraBirimi = ParaBirimi.TL;
	private JLabel lblGenel;
	private JLabel lblKdv;
	private JLabel lblKdvDahil;

	public TeklifTablePanel() {

		setLayout(new BorderLayout());

		String[] kolonlar = {

				"<html>SIRA<br>NO</html>", "<html>ÜRÜN KODU</html>", "<html>ÜRÜN ADI</html>",

				"<html>YÜKSEKLİK<br>NET İÇ<hr>W(mm)</html>", "<html>GENİŞLİK<br>NET İÇ<hr>H(mm)</html>",
				"<html>UZUNLUK<br>NET İÇ<hr>L(mm)</html>", "<html>ÇAP<br>NET İÇ<hr>Ø(mm)</html>",

				"<html>ÇERÇEVE<br>TİPİ</html>", "<html>DAMPER<br>TİPİ</html>", "<html>RAL<br>KODU</html>",
				"<html>MONTAJ<br>ŞEKLİ</html>",

				"<html>MİKTAR</html>", "<html>BİRİM</html>", "<html>BİRİM<br>FİYAT</html>",
				"<html>TOPLAM<br>FİYAT</html>" };

		model = new DefaultTableModel(kolonlar, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(model);

		// ⭐ Model değişince (satır ekleme/güncelleme) tüm satır yüksekliklerini ayarla
		model.addTableModelListener(e -> SwingUtilities.invokeLater(this::adjustAllRowHeights));

		// ⭐ Ürün adı kolonunun genişliği değişince tekrar hesapla (kolon çekip
		// büyüt-küçült)
		table.getColumnModel().getColumn(2).addPropertyChangeListener(evt -> {
			if ("width".equals(evt.getPropertyName())) {
				SwingUtilities.invokeLater(this::adjustAllRowHeights);
			}
		});

		// ⭐ ÜRÜN ADI kolonu: satır kaydırma (wrap) renderer
		table.getColumnModel().getColumn(2).setCellRenderer(new MultiLineTextRenderer());

		// Daha iyi görünüm için minimum satır yüksekliği
		table.setRowHeight(28);

		DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {

			@Override
			protected void setValue(Object value) {

				if (value == null) {
					setText("");
					return;
				}

				double v;

				// model bazen Double, bazen String atayabilir → ikisini de yakala
				if (value instanceof Number) {
					v = ((Number) value).doubleValue();
				} else {
					try {
						v = Double.parseDouble(value.toString());
					} catch (Exception ex) {
						super.setValue(value);
						return;
					}
				}

				String symbol = paraBirimi.getSymbol();

				double shown = KurService.cevir(v, paraBirimi); // ⭐ TL → Seçili para birimi

				setHorizontalAlignment(SwingConstants.RIGHT);
				setText(String.format("%.2f%s", shown, symbol));
			}
		};

		// =====================================================
		// ⭐ FIT MODE (panel içine sığdır)
		// =====================================================

		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		// multi-line header için yükseklik
		table.getTableHeader().setPreferredSize(new Dimension(0, 65));

		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		// =====================================================
		// ⭐ FIT MODE GENİŞLİKLER (daha dar)
		// =====================================================

		// SIRA NO
		table.getColumnModel().getColumn(0).setPreferredWidth(45);

		// ÜRÜN KODU
		table.getColumnModel().getColumn(1).setPreferredWidth(110);

		// ÜRÜN ADI — GENİŞ
		table.getColumnModel().getColumn(2).setPreferredWidth(360);

		// ÖLÇÜLER — DAR
		table.getColumnModel().getColumn(3).setPreferredWidth(55); // W
		table.getColumnModel().getColumn(4).setPreferredWidth(55); // H
		table.getColumnModel().getColumn(5).setPreferredWidth(55); // L
		table.getColumnModel().getColumn(6).setPreferredWidth(55); // Ø

		table.getColumnModel().getColumn(7).setPreferredWidth(95);  // Çerçeve
		table.getColumnModel().getColumn(8).setPreferredWidth(95);  // Damper
		table.getColumnModel().getColumn(9).setPreferredWidth(90);  // RAL
		table.getColumnModel().getColumn(10).setPreferredWidth(90); // Montaj

		table.getColumnModel().getColumn(11).setPreferredWidth(70); // Miktar
		table.getColumnModel().getColumn(12).setPreferredWidth(70); // Birim

		table.getColumnModel().getColumn(13).setPreferredWidth(95);  // Birim Fiyat
		table.getColumnModel().getColumn(14).setPreferredWidth(105); // Toplam Fiyat

		table.getColumnModel().getColumn(13).setCellRenderer(priceRenderer); // Birim Fiyat
		table.getColumnModel().getColumn(14).setCellRenderer(priceRenderer); // Toplam Fiyat
		
		JScrollPane scroll = new JScrollPane(table);
		add(scroll, BorderLayout.CENTER);

		// =====================================================
		// ⭐ SAĞ ALT TOPLAM PANELİ (ERP STYLE)
		// =====================================================

		JPanel southContainer = new JPanel(new BorderLayout());

		Font normalFont = new Font("Segoe UI", Font.PLAIN, 13);
		Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
		Font totalFont = new Font("Segoe UI", Font.BOLD, 16);

		// ---------------------
		// TOPLAM PANEL
		// ---------------------
		JPanel toplamPanel = new JPanel();
		toplamPanel.setLayout(new BoxLayout(toplamPanel, BoxLayout.Y_AXIS));
		toplamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));

		JLabel lblGenelText = new JLabel("GENEL TOPLAM");
		JLabel lblKdvText = new JLabel("KDV %20");
		JLabel lblKdvDahilText = new JLabel("KDV DAHİL GENEL");

		lblGenel = new JLabel("0.00");
		lblKdv = new JLabel("0.00");
		lblKdvDahil = new JLabel("0.00");

		lblGenel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblKdv.setHorizontalAlignment(SwingConstants.RIGHT);
		lblKdvDahil.setHorizontalAlignment(SwingConstants.RIGHT);

		lblGenelText.setFont(normalFont);
		lblKdvText.setFont(normalFont);
		lblKdvDahilText.setFont(boldFont);

		lblGenel.setFont(normalFont);
		lblKdv.setFont(normalFont);
		lblKdvDahil.setFont(totalFont);

		toplamPanel.add(createRow(lblGenelText, lblGenel));
		toplamPanel.add(createRow(lblKdvText, lblKdv));

		JSeparator line = new JSeparator();
		line.setMaximumSize(new Dimension(240, 1));
		toplamPanel.add(line);

		toplamPanel.add(createRow(lblKdvDahilText, lblKdvDahil));

		// sağa yasla
		JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightWrapper.add(toplamPanel);

		// ---------------------
		// EXCEL BUTONU
		// ---------------------
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnExcel = new JButton("Excel'e Aktar");
		buttonPanel.add(btnExcel);

		southContainer.add(rightWrapper, BorderLayout.CENTER);
		southContainer.add(buttonPanel, BorderLayout.SOUTH);

		add(southContainer, BorderLayout.SOUTH);

		btnExcel.addActionListener(e -> {
			try {
				// Şimdilik sabit TL gönderiyoruz.
				// İstersen bunu sonradan Workspace'ten seçilen para birimini alacak şekilde
				// yaparız.
				ExcelExporter.export(table, paraBirimi);

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Excel aktarım hatası: " + ex.getMessage(), "Hata",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	// =====================================================

	public void addRow(Object[] data) {

		int sıraNo = model.getRowCount() + 1;

		Object[] row = new Object[data.length + 1];

		row[0] = sıraNo;

		for (int i = 0; i < data.length; i++) {
			row[i + 1] = data[i];
		}

		model.addRow(row);

		adjustAllRowHeights();

		hesaplaToplamPanel();

	}

	public void removeSelected() {

		int r = table.getSelectedRow();

		if (r == -1)
			return;

		Object col1 = model.getValueAt(r, 2);
		String txt = (col1 == null) ? "" : col1.toString().trim();

		model.removeRow(r);

		refreshRowNumbers();

		hesaplaToplamPanel();

	}

	private void refreshRowNumbers() {

		int sıra = 1;

		for (int i = 0; i < model.getRowCount(); i++) {

			Object col1 = model.getValueAt(i, 2);
			String txt = (col1 == null) ? "" : col1.toString().trim();

			boolean isSummary = txt.equals("GENEL TOPLAM") || txt.equals("KDV %20") || txt.equals("KDV DAHİL GENEL");

			// boş satır (summary boşluğu)
			boolean isBlank = (txt.isEmpty());

			if (isSummary || isBlank) {
				model.setValueAt("", i, 0); // sıra no boş bırak
			} else {
				model.setValueAt(sıra++, i, 0);
			}
		}
	}

	public void setParaBirimi(ParaBirimi pb) {
		this.paraBirimi = (pb == null) ? ParaBirimi.TL : pb;

		model.fireTableDataChanged(); // ⭐ renderer tekrar çalışır
		table.repaint();

		hesaplaToplamPanel();
	}

	public JTable getTable() {
		return table;
	}

	private void hesaplaToplamPanel() {

		double toplam = 0;

		for (int i = 0; i < model.getRowCount(); i++) {

			Object val = model.getValueAt(i, 14);

			if (val == null)
				continue;

			try {
				if (val instanceof Number) {
					toplam += ((Number) val).doubleValue();
				}
			} catch (Exception ignore) {
			}
		}

		double kdv = toplam * 0.20;
		double dahil = toplam + kdv;

		String symbol = paraBirimi.getSymbol();

		double shownToplam = KurService.cevir(toplam, paraBirimi);
		double shownKdv = KurService.cevir(kdv, paraBirimi);
		double shownDahil = KurService.cevir(dahil, paraBirimi);

		lblGenel.setText(String.format("%.2f%s", shownToplam, symbol));
		lblKdv.setText(String.format("%.2f%s", shownKdv, symbol));
		lblKdvDahil.setText(String.format("%.2f%s", shownDahil, symbol));
	}

	private JPanel createRow(JLabel left, JLabel right) {

		JPanel row = new JPanel(new BorderLayout());
		row.setMaximumSize(new Dimension(260, 25));

		left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		row.add(left, BorderLayout.WEST);
		row.add(right, BorderLayout.EAST);

		return row;
	}

	// =====================================================
	// ⭐ HTML içeriğe göre satır yüksekliği ayarla
	// =====================================================
	private void adjustRowHeight(int row) {

		int col = 2; // ÜRÜN ADI kolonu

		TableCellRenderer renderer = table.getCellRenderer(row, col);
		Component comp = table.prepareRenderer(renderer, row, col);

		int width = table.getColumnModel().getColumn(col).getWidth();

		// ⭐ Kritik: JTextArea'nın yüksekliği doğru hesaplaması için genişliği set et
		if (comp instanceof JTextArea) {
			((JTextArea) comp).setSize(width, Short.MAX_VALUE);
		} else {
			comp.setSize(width, Short.MAX_VALUE);
		}

		int prefH = comp.getPreferredSize().height;

		// küçük bir pay + min yükseklik
		int finalH = Math.max(28, prefH + 2);

		if (table.getRowHeight(row) != finalH) {
			table.setRowHeight(row, finalH);
		}
	}

	// =====================================================
	// ⭐ ÜRÜN ADI için çok satırlı renderer (wrap)
	// =====================================================
	private class MultiLineTextRenderer extends JTextArea implements TableCellRenderer {

		public MultiLineTextRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6)); // iç boşluk
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			String text = (value == null) ? "" : value.toString();

			// Eğer <html> / <br> vb. geliyorsa normal yazıya çevir
			text = text.replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("(?i)</html>", "").replaceAll("(?i)<html>", "")
					.replaceAll("(?i)<hr\\s*/?>", "\n").replaceAll("(?i)<[^>]+>", ""); // diğer tag’leri temizle

			setText(text);

			// font/renk seçimi JTable ile uyumlu
			setFont(table.getFont());

			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			// ⭐ Kritik: yüksekliği doğru hesaplamak için kolon genişliğini veriyoruz
			int colWidth = table.getColumnModel().getColumn(column).getWidth();
			setSize(new Dimension(colWidth, Short.MAX_VALUE));

			return this;
		}
	}

	private void adjustAllRowHeights() {
		for (int r = 0; r < table.getRowCount(); r++) {
			adjustRowHeight(r);
		}
	}
}
