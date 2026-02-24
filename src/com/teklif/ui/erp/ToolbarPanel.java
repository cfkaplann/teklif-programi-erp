package com.teklif.ui.erp;

import javax.swing.*;
import java.awt.*;

import com.teklif.model.ParaBirimi;
import com.teklif.ui.util.PlaceholderComboRenderer;

public class ToolbarPanel extends JPanel {

    private JComboBox<String> cmbKategori;
    private JComboBox<Object> cmbUrun;
    private JComboBox<ParaBirimi> cmbParaBirimi;

    public ToolbarPanel(){

        setLayout(new FlowLayout(FlowLayout.LEFT));

        cmbKategori = new JComboBox<>();
        cmbUrun     = new JComboBox<>();

        cmbKategori.addItem("Kategori Seçiniz");
        cmbUrun.addItem("Ürün Seçiniz");

        cmbKategori.setRenderer(new PlaceholderComboRenderer("Kategori Seçiniz"));
        cmbUrun.setRenderer(new PlaceholderComboRenderer("Ürün Seçiniz"));

        add(new JLabel("Kategori"));
        add(cmbKategori);

        add(new JLabel("Ürün"));
        add(cmbUrun);

        cmbParaBirimi = new JComboBox<>(ParaBirimi.values());
        cmbParaBirimi.setSelectedItem(ParaBirimi.TL);

        add(new JLabel("Para Birimi"));
        add(cmbParaBirimi);
    }

    public JComboBox<String> getCmbKategori(){ return cmbKategori; }
    public JComboBox<Object> getCmbUrun(){ return cmbUrun; }
    public JComboBox<ParaBirimi> getCmbParaBirimi() {
        return cmbParaBirimi;
    }
}
