package com.teklif.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TeklifTablePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public TeklifTablePanel() {

        setLayout(new BorderLayout());

        String[] kolonlar = {

                "<html>SIRA<br>NO</html>",
                "<html>ÜRÜN ADI</html>",

                "<html>YÜKSEKLİK<br>NET İÇ<hr>W(mm)</html>",
                "<html>GENİŞLİK<br>NET İÇ<hr>H(mm)</html>",
                "<html>UZUNLUK<br>NET İÇ<hr>L(mm)</html>",
                "<html>ÇAP<br>NET İÇ<hr>Ø(mm)</html>",

                "<html>ÇERÇEVE<br>TİPİ</html>",
                "<html>DAMPER<br>TİPİ</html>",
                "<html>RAL<br>KODU</html>",
                "<html>MONTAJ<br>ŞEKLİ</html>",

                "<html>MİKTAR</html>",
                "<html>BİRİM</html>",
                "<html>BİRİM<br>FİYAT</html>",
                "<html>TOPLAM<br>FİYAT</html>"
        };

        model = new DefaultTableModel(kolonlar,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        table = new JTable(model);

        // =====================================================
        // ⭐ FIT MODE (panel içine sığdır)
        // =====================================================

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // multi-line header için yükseklik
        table.getTableHeader().setPreferredSize(new Dimension(0,65));

        ((DefaultTableCellRenderer)
                table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

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
        add(scroll,BorderLayout.CENTER);
    }

    // =====================================================

    public void addRow(Object[] data){

        int sıraNo = model.getRowCount() + 1;

        Object[] row = new Object[data.length+1];

        row[0] = sıraNo;

        for(int i=0;i<data.length;i++){
            row[i+1] = data[i];
        }

        model.addRow(row);
    }

    public void removeSelected(){

        int r = table.getSelectedRow();

        if(r==-1) return;

        model.removeRow(r);

        refreshRowNumbers();
    }

    private void refreshRowNumbers(){

        for(int i=0;i<model.getRowCount();i++){
            model.setValueAt(i+1,i,0);
        }
    }

    public JTable getTable(){
        return table;
    }
}
