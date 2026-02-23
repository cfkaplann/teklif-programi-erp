package com.teklif.ui.table;

import javax.swing.*;
import com.teklif.export.ExcelExporter;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TeklifTablePanel extends JPanel {

	private JTable table;
	private DefaultTableModel model;

	private JLabel lblGenel;
	private JLabel lblKdv;
	private JLabel lblKdvDahil;

	public TeklifTablePanel() {

		setLayout(new BorderLayout());

		String[] kolonlar = {

				"<html>SIRA<br>NO</html>", "<html>ÜRÜN ADI</html>",

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

		// =====================================================
		// ⭐ FIT MODE (panel içine sığdır)
		// =====================================================

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// multi-line header için yükseklik
		table.getTableHeader().setPreferredSize(new Dimension(0, 65));

		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		// =====================================================
		// ⭐ FIT MODE GENİŞLİKLER (daha dar)
		// =====================================================

		table.getColumnModel().getColumn(0).setPreferredWidth(45);
		table.getColumnModel().getColumn(1).setPreferredWidth(160);

		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(4).setPreferredWidth(80);
		table.getColumnModel().getColumn(5).setPreferredWidth(80);

		table.getColumnModel().getColumn(6).setPreferredWidth(95);
		table.getColumnModel().getColumn(7).setPreferredWidth(95);
		table.getColumnModel().getColumn(8).setPreferredWidth(90);
		table.getColumnModel().getColumn(9).setPreferredWidth(90);

		table.getColumnModel().getColumn(10).setPreferredWidth(70);
		table.getColumnModel().getColumn(11).setPreferredWidth(70);

		table.getColumnModel().getColumn(12).setPreferredWidth(95);
		table.getColumnModel().getColumn(13).setPreferredWidth(105);

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
		line.setMaximumSize(new Dimension(240,1));
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
				ExcelExporter.export(table);

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
		hesaplaToplamPanel();

	}

	public void removeSelected() {

		int r = table.getSelectedRow();

		if (r == -1)
			return;

		Object col1 = model.getValueAt(r, 1);
		String txt = (col1 == null) ? "" : col1.toString().trim();

		model.removeRow(r);

		refreshRowNumbers();

		hesaplaToplamPanel();

	}

	private void refreshRowNumbers() {

		int sıra = 1;

		for (int i = 0; i < model.getRowCount(); i++) {

			Object col1 = model.getValueAt(i, 1);
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

	public JTable getTable() {
		return table;
	}

	private void hesaplaToplamPanel() {

		double toplam = 0;

		for (int i = 0; i < model.getRowCount(); i++) {

			Object val = model.getValueAt(i, 13);

			if (val == null)
				continue;

			try {
				toplam += Double.parseDouble(val.toString());
			} catch (Exception ignore) {
			}
		}

		double kdv = toplam * 0.20;
		double dahil = toplam + kdv;

		lblGenel.setText(String.format("%.2f", toplam));
		lblKdv.setText(String.format("%.2f", kdv));
		lblKdvDahil.setText(String.format("%.2f", dahil));
	}
	
	private JPanel createRow(JLabel left, JLabel right){

	    JPanel row = new JPanel(new BorderLayout());
	    row.setMaximumSize(new Dimension(260,25));

	    left.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));

	    row.add(left,BorderLayout.WEST);
	    row.add(right,BorderLayout.EAST);

	    return row;
	}
}
