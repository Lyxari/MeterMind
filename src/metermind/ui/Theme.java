package metermind.ui;

import java.awt.*;

/**
 * Central design system for MeterMind's dark theme UI.
 * Contains all color tokens, fonts, dimensions, and rendering utilities.
 */
public final class Theme {

    // ========================
    //   Color Palette
    // ========================

    /** Plain White — main background */
    public static final Color BG_PRIMARY = new Color(0xFFFFFF);
    /** Off-White — card/panel surface */
    public static final Color BG_SURFACE = new Color(0xF8FAFC);
    /** Light Gray — hover states, borders */
    public static final Color BG_HOVER = new Color(0xF1F5F9);
    /** Gray — subtle borders, dividers */
    public static final Color BORDER = new Color(0xE2E8F0);

    /** Electric Teal — primary accent */
    public static final Color PRIMARY = new Color(0x0EA5E9);
    /** Primary darkened for hover */
    public static final Color PRIMARY_HOVER = new Color(0x0284C7);
    /** Primary with transparency for backgrounds */
    public static final Color PRIMARY_BG = new Color(0x0EA5E9, true) { 
        // 15% opacity version
    };

    /** Emerald — success, positive changes, paid status */
    public static final Color SUCCESS = new Color(0x10B981);
    public static final Color SUCCESS_BG = new Color(16, 185, 129, 30);

    /** Amber — warnings, high-bill flags */
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color WARNING_BG = new Color(245, 158, 11, 30);

    /** Crimson — danger, overdue, over-budget */
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color DANGER_BG = new Color(239, 68, 68, 30);

    /** Text Colors */
    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x475569);
    public static final Color TEXT_MUTED = new Color(0x64748B);

    // ========================
    //   Typography
    // ========================

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_KPI_VALUE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_KPI_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NAV = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NAV_ACTIVE = new Font("Segoe UI", Font.BOLD, 14);

    // ========================
    //   Dimensions
    // ========================

    public static final int SIDEBAR_WIDTH = 220;
    public static final int CARD_PADDING = 20;
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_RADIUS = 8;
    public static final int SPACING = 16;
    public static final int SPACING_SM = 8;
    public static final int SPACING_LG = 24;

    private Theme() {
        // Utility class — no instantiation
    }

    // ========================
    //   Rendering Utilities
    // ========================

    /**
     * Enables anti-aliasing and high-quality rendering on a Graphics2D context.
     */
    public static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Paints a rounded rectangle card background.
     */
    public static void paintCard(Graphics2D g2, int x, int y, int w, int h) {
        paintCard(g2, x, y, w, h, BG_SURFACE);
    }

    /**
     * Paints a rounded rectangle card with a custom background color.
     */
    public static void paintCard(Graphics2D g2, int x, int y, int w, int h, Color bg) {
        enableAntiAliasing(g2);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 15));
        g2.fillRoundRect(x + 1, y + 1, w, h, CARD_RADIUS, CARD_RADIUS);

        // Card background
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);

        // Subtle border
        g2.setColor(BORDER);
        g2.drawRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
    }

    /**
     * Paints a horizontal gradient bar (used for progress bars).
     */
    public static void paintGradientBar(Graphics2D g2, int x, int y, int w, int h,
                                         Color startColor, Color endColor, double fillPercent) {
        enableAntiAliasing(g2);

        // Background track
        g2.setColor(BG_HOVER);
        g2.fillRoundRect(x, y, w, h, h, h);

        // Filled portion
        int fillWidth = (int) (w * Math.min(1.0, fillPercent / 100.0));
        if (fillWidth > 0) {
            GradientPaint gp = new GradientPaint(x, y, startColor, x + fillWidth, y, endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, fillWidth, h, h, h);
        }
    }

    /**
     * Returns the appropriate color for a percentage change value.
     * Positive = danger (cost increased), Negative = success (cost decreased), Zero = secondary.
     */
    public static Color getChangeColor(double percentChange) {
        if (percentChange > 0) return DANGER;
        if (percentChange < 0) return SUCCESS;
        return TEXT_SECONDARY;
    }
}
