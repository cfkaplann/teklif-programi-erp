package com.teklif.ui.workspace;

import com.teklif.model.dto.PricingRequest;

import com.teklif.ui.mapper.OlcuRequestMapper;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;
import com.teklif.repository.UrunKataloguDeposu;

import com.teklif.ui.component.OlcuComponent;
import com.teklif.ui.component.RalCompositeComponent;
import com.teklif.ui.erp.ActionBarPanel;
import com.teklif.ui.erp.DynamicFormPanel;
import com.teklif.ui.erp.ToolbarPanel;
import com.teklif.ui.factory.OlcuPanelFactory;
import com.teklif.ui.factory.OzellikPanelFactory;
import com.teklif.ui.table.TeklifTablePanel;

import com.teklif.controller.TeklifController;
import com.teklif.pricing.dto.PricingResult;
import com.teklif.ui.mapper.OzellikRequestMapper;

public class TeklifWorkspace extends JPanel {

	private final ToolbarPanel toolbar;
	private final DynamicFormPanel form;
	private final ActionBarPanel actions;
	private final TeklifTablePanel table;
	private TeklifController controller = new TeklifController();

	private final List<OlcuComponent> olcuComponents = new ArrayList<>();
	private final Map<OzellikTipi, JComponent> ozellikInputs = new HashMap<>();

	private JTextField txtMiktar;

	public TeklifWorkspace() {

		setLayout(new BorderLayout());

		toolbar = new ToolbarPanel();
		form = new DynamicFormPanel();
		actions = new ActionBarPanel();
		table = new TeklifTablePanel();

		// ⭐ başlangıçta gizli
		form.setVisible(false);
		actions.setVisible(false);

		add(toolbar, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout());
		center.add(form, BorderLayout.CENTER);
		center.add(actions, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, table);

		split.setResizeWeight(0.65);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);
		split.setDividerSize(4);

		// ⭐ PROGRAM AÇILIŞ YÜKSEKLİĞİ
		SwingUtilities.invokeLater(() -> {
		    split.setDividerLocation(175); // burayı istediğin kadar büyüt
		});

		add(split, BorderLayout.CENTER);


		initKategori();

		toolbar.getCmbKategori().addActionListener(e -> kategoriDegisti());
		toolbar.getCmbUrun().addActionListener(e -> urunDegisti());

		actions.getBtnUrunEkle().addActionListener(e -> urunEkle());
		actions.getBtnUrunSil().addActionListener(e -> table.removeSelected());
	}

	// =====================================================
	// ⭐ KATEGORI INIT
	// =====================================================

	private void initKategori() {

		toolbar.getCmbKategori().removeAllItems();

		toolbar.getCmbKategori().addItem("Kategori Seçiniz");

		for (UrunKategori k : UrunKataloguDeposu.tumKategoriler()) {
			toolbar.getCmbKategori().addItem(k.name());
		}

		toolbar.getCmbUrun().removeAllItems();
		toolbar.getCmbUrun().addItem("Ürün Seçiniz");
		toolbar.getCmbUrun().setEnabled(false);
	}

	// =====================================================
	// ⭐ KATEGORI DEĞİŞTİ
	// =====================================================

	private void kategoriDegisti() {

		toolbar.getCmbUrun().removeAllItems();
		toolbar.getCmbUrun().addItem("Ürün Seçiniz");

		String kat = (String) toolbar.getCmbKategori().getSelectedItem();

		// ⭐ KATEGORİ SEÇİNİZ seçildi → HER ŞEY RESET
		if (kat == null || kat.equals("Kategori Seçiniz")) {

			toolbar.getCmbUrun().setEnabled(false);

			// ⭐ FORM RESET
			form.getPnlOlculer().removeAll();
			form.getPnlTeknik().removeAll();
			form.getPnlRalAksesuar().removeAll();

			olcuComponents.clear();
			ozellikInputs.clear();

			form.setVisible(false);
			actions.setVisible(false);

			revalidate();
			repaint();

			return;
		}

		toolbar.getCmbUrun().setEnabled(true);

		for (UrunKart u : UrunKataloguDeposu.tumUrunler()) {
			if (u.getKategori().name().equals(kat)) {
				toolbar.getCmbUrun().addItem(u);
			}
		}
	}

	// =====================================================
	// ⭐ URUN DEĞİŞTİ
	// =====================================================

	private void urunDegisti() {

		Object selected = toolbar.getCmbUrun().getSelectedItem();

		// ⭐ Ürün Seçiniz ghost → form kapalı
		if (!(selected instanceof UrunKart)) {
			form.setVisible(false);
			actions.setVisible(false);
			return;
		}

		UrunKart kart = (UrunKart) selected;

		form.setVisible(true);
		actions.setVisible(true);

		rebuildForm(kart);
	}

	// =====================================================
	// ⭐ FORM BUILD
	// =====================================================

	private void rebuildForm(UrunKart kart) {

		JPanel pnlOlcu = form.getPnlOlculer();
		JPanel pnlTeknik = form.getPnlTeknik();
		JPanel pnlRal = form.getPnlRalAksesuar();

		pnlOlcu.removeAll();
		pnlTeknik.removeAll();
		pnlRal.removeAll();

		olcuComponents.clear();
		ozellikInputs.clear();

		// =====================================================
		// ⭐ ÖLÇÜLER
		// =====================================================

		OlcuComponent kasaComp = null;
		OlcuComponent bogazComp = null;

		for (OlcuAlanTipi tip : kart.getZorunluOlculer()) {

			OlcuComponent comp = OlcuPanelFactory.createComponent(tip);

			// ⭐ CONFIGTEN COMBO DEĞERLERİ BAS
			if (kart.getIzinliOlcuDegerleri() != null) {

				java.util.List<String> values = kart.getIzinliOlcuDegerleri().get(tip);

				if (values != null) {
					comp.setAllowedValues(values);
				}
			}

			// ⭐ REFERANS YAKALA
			if (tip == OlcuAlanTipi.KASA_WH)
				kasaComp = comp;
			if (tip == OlcuAlanTipi.BOGAZ_WH)
				bogazComp = comp;

			pnlOlcu.add(comp);
			olcuComponents.add(comp);
		}

		// =====================================================
		// ⭐ KASA → BOĞAZ FİLTRE SİSTEMİ
		// =====================================================

		if (kasaComp != null && bogazComp != null && kart.getBogazFiltreMap() != null
				&& kart.getZorunluOlculer().contains(OlcuAlanTipi.BOGAZ_WH)) {

			OlcuComponent finalKasa = kasaComp;
			OlcuComponent finalBogaz = bogazComp;

			Runnable filtreUygula = () -> {

				String secilenKasa = finalKasa.getValue();

				List<String> izinliBogaz = kart.getBogazFiltreMap().get(secilenKasa);

				if (izinliBogaz != null) {
					finalBogaz.setAllowedValues(izinliBogaz);
				}
			};

			// ⭐ kullanıcı değiştirince
			finalKasa.addChangeListener(filtreUygula);

			// ⭐ ürün ilk açıldığında da tetikle
			SwingUtilities.invokeLater(filtreUygula);

			// ⭐⭐⭐ BOĞAZ DEFAULT SEÇİM GARANTİ ⭐⭐⭐
			SwingUtilities.invokeLater(() -> {

				String val = finalBogaz.getValue();

				if (val == null || val.isBlank()) {

					List<String> tumBogaz = (kart.getIzinliOlcuDegerleri() == null) ? null
							: kart.getIzinliOlcuDegerleri().get(OlcuAlanTipi.BOGAZ_WH);

					if (tumBogaz != null && !tumBogaz.isEmpty()) {
						finalBogaz.setValue(tumBogaz.get(0));
					}
				}
			});

		}

		// =====================================================
		// ⭐ MİKTAR ALANI
		// =====================================================

		JPanel miktarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel lbl = new JLabel("Miktar");
		lbl.setPreferredSize(new Dimension(95, 22));

		gbc.gridx = 0;
		miktarPanel.add(lbl, gbc);

		txtMiktar = new JTextField();
		txtMiktar.setPreferredSize(new Dimension(75, 22));

		gbc.gridx = 1;
		miktarPanel.add(txtMiktar, gbc);

		pnlOlcu.add(miktarPanel);

		txtMiktar.setDocument(new javax.swing.text.PlainDocument() {
			@Override
			public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
					throws javax.swing.text.BadLocationException {

				if (str == null)
					return;

				if (str.matches("[0-9]*")) {
					super.insertString(offs, str, a);
				}
			}
		});

		// =====================================================
		// ⭐ ÖZELLİKLER
		// =====================================================

		ozellikInputs.putAll(OzellikPanelFactory.createERPColumns(pnlTeknik, pnlRal, kart, () -> {
		}));

		revalidate();
		repaint();
	}

	// =====================================================
	// ⭐ URUN EKLE
	// =====================================================

	private void urunEkle() {

		this.requestFocusInWindow();

		if (!formValidMi()) {
			JOptionPane.showMessageDialog(this, "Tüm alanları doldurmadan ürün ekleyemezsiniz.", "Eksik Bilgi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Object selected = toolbar.getCmbUrun().getSelectedItem();

		if (!(selected instanceof UrunKart))
			return;

		UrunKart kart = (UrunKart) selected;

		String miktarStr = txtMiktar.getText();

		int miktar = Integer.parseInt(miktarStr);

		if (miktar <= 0)
			throw new RuntimeException("Miktar 0'dan büyük olmalı");

		// =====================================================
		// ⭐ PRICING REQUEST BUILD
		// =====================================================

		PricingRequest.Builder builder = PricingRequest.builder(kart.getKod());

		// ölçüler mapper
		OlcuRequestMapper.map(builder, olcuComponents);

		PricingRequest req = builder.build();

		// =====================================================
		// ⭐ ÖZELLİK MAP
		// =====================================================

		Map<OzellikTipi, List<String>> secimler = OzellikRequestMapper.map(ozellikInputs);

		System.out.println(secimler);

		// =====================================================
		// ⭐ CONTROLLER ÜZERİNDEN HESAPLA
		// =====================================================

		PricingResult result = controller.hesapla(req, secimler);

		double birimFiyat = result.getToplam();
		double toplamFiyat = birimFiyat * miktar;

		String genislik = "";
		String yukseklik = "";
		String uzunluk = "";
		String cap = "";

		for (OlcuComponent c : olcuComponents) {

			if (c.getTip() == OlcuAlanTipi.GENISLIK)
				genislik = c.getValue();

			if (c.getTip() == OlcuAlanTipi.YUKSEKLIK)
				yukseklik = c.getValue();

			if (c.getTip() == OlcuAlanTipi.UZUNLUK)
				uzunluk = c.getValue();

			if (c.getTip() == OlcuAlanTipi.CAP || c.getTip() == OlcuAlanTipi.BOGAZ_CAP
					|| c.getTip() == OlcuAlanTipi.NETIC_CAP)
				cap = c.getValue();
		}

		String cerceve = "";
		String damper = "";
		String ral = "";
		String montaj = "";

		for (Map.Entry<OzellikTipi, JComponent> e : ozellikInputs.entrySet()) {

			JComponent comp = e.getValue();

			// ⭐ NORMAL COMBO'LAR
			if (comp instanceof JComboBox) {

				Object val = ((JComboBox<?>) comp).getSelectedItem();

				if (val == null)
					continue;

				switch (e.getKey()) {

				case CERCEVE_TIPI:
					cerceve = val.toString();
					break;
				case DAMPER_TIPI:
					damper = val.toString();
					break;
				case MONTAJ:
					montaj = val.toString();
					break;
				}
			}

			// ⭐ YENİ RAL COMPOSITE COMPONENT
			if (e.getKey() == OzellikTipi.RAL && comp instanceof RalCompositeComponent) {

				RalCompositeComponent ralComp = (RalCompositeComponent) comp;

				ral = ralComp.getValue(); // ⭐ Boyalı - 9010 burada oluşuyor
			}
		}

		Object[] row = { kart.getAd(), genislik, yukseklik, uzunluk, cap, cerceve, damper, ral, montaj, miktarStr,
				"Adet", birimFiyat, toplamFiyat };

		table.addRow(row);
	}

	// =====================================================
	// ⭐ VALIDATION
	// =====================================================

	private boolean formValidMi() {

		for (OlcuComponent c : olcuComponents) {

			String v = c.getValue();

			if (v == null || v.isBlank())
				return false;

			// ✅ KASA/BOĞAZ string olduğu için sayı kontrolü YAPMA
			if (c.getTip() == OlcuAlanTipi.KASA_WH || c.getTip() == OlcuAlanTipi.BOGAZ_WH
					|| c.getTip() == OlcuAlanTipi.KASA_CAP) {
				continue;
			}

			// ✅ Diğerleri sayısal (W,H,L,Ø,slot vs.)
			try {
				if (c.getDoubleValue() <= 0)
					return false;
			} catch (Exception ex) {
				return false;
			}
		}

		// ✅ Miktar kontrolü
		if (txtMiktar == null || txtMiktar.getText().isBlank())
			return false;

		try {
			if (Integer.parseInt(txtMiktar.getText()) <= 0)
				return false;
		} catch (Exception e) {
			return false;
		}

		// ✅ Combo özellikler boş mu
		for (JComponent comp : ozellikInputs.values()) {

			if (comp instanceof JComboBox) {

				Object val = ((JComboBox<?>) comp).getSelectedItem();

				if (val == null || "Seçim Yapınız".equals(val.toString()))
					return false;
			}

			if (comp instanceof RalCompositeComponent) {

				RalCompositeComponent rc = (RalCompositeComponent) comp;

				String val = rc.getValue();

				if (val == null || val.isBlank())
					return false;

				// ⭐ BOYALI ise RAL kodu zorunlu
				if ("Boyalı".equalsIgnoreCase(rc.getRalSecim())
						&& (rc.getRalKod() == null || rc.getRalKod().isBlank())) {
					return false;
				}
			}

		}

		return true;
	}

}
