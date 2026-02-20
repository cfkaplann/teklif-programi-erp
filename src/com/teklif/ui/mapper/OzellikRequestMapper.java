package com.teklif.ui.mapper;

import javax.swing.*;
import java.awt.Component;
import java.util.*;

import com.teklif.model.OzellikTipi;

public class OzellikRequestMapper {

    public static Map<OzellikTipi, List<String>> map(
            Map<OzellikTipi, JComponent> inputs){

        Map<OzellikTipi,List<String>> result = new HashMap<>();

        for(OzellikTipi tip : inputs.keySet()){

            JComponent comp = inputs.get(tip);

            // ===============================
            // SINGLE SELECT → COMBOBOX
            // ===============================
            if(comp instanceof JComboBox){

                Object val = ((JComboBox<?>) comp).getSelectedItem();

                if(val != null && !"Seçim Yapınız".equals(val.toString())){
                    result.put(tip, List.of(val.toString()));
                }
            }

            // ===============================
            // MULTI SELECT → CHECKBOX PANEL
            // ===============================
            else if(comp instanceof JPanel){

                List<String> secili = new ArrayList<>();

                for(Component c : ((JPanel) comp).getComponents()){

                	if(c instanceof AbstractButton){

                	    AbstractButton btn = (AbstractButton)c;

                	    if(btn.isSelected()){
                	        secili.add(btn.getText());
                	    }
                	}

                }

                if(!secili.isEmpty()){
                    result.put(tip, secili);
                }
            }
        }

        return result;
    }
}
