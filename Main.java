package metermind;

import metermind.ui.MeterMindFrame;
import metermind.ui.Theme;

import javax.swing.*;

/**
 * Entry point for the MeterMind application.
 */
public class Main {
    public static void main(String[] args) {
        // Set the system look and feel for native window decorations
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel.");
        }

        // Global UI Manager overrides for cleaner Swing components
        UIManager.put("Panel.background", Theme.BG_PRIMARY);
        UIManager.put("OptionPane.background", Theme.BG_PRIMARY);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);
        UIManager.put("Button.background", Theme.BG_SURFACE);
        UIManager.put("Button.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("Label.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.background", Theme.BG_SURFACE);
        UIManager.put("ComboBox.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("TextField.background", Theme.BG_SURFACE);
        UIManager.put("TextField.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("Spinner.background", Theme.BG_SURFACE);
        UIManager.put("Spinner.foreground", Theme.TEXT_PRIMARY);

        // Ensure UI creation happens on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MeterMindFrame frame = new MeterMindFrame();
            frame.setVisible(true);
        });
    }
}
