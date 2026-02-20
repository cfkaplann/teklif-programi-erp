package com.teklif;

import javax.swing.SwingUtilities;
import com.teklif.ui.MainFrame;

public class App {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
