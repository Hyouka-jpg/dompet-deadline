package id.ac.usk.dompetdeadline.view;

import java.awt.Color;
import java.awt.Font;

public final class Theme {
    public static final Color BACKGROUND = new Color(247, 249, 251);
    public static final Color SIDEBAR = new Color(21, 37, 57);
    public static final Color SIDEBAR_MUTED = new Color(152, 169, 185);
    public static final Color PRIMARY = new Color(24, 117, 113);
    public static final Color PRIMARY_LIGHT = new Color(231, 246, 243);
    public static final Color TEXT = new Color(29, 43, 58);
    public static final Color MUTED = new Color(103, 120, 136);
    public static final Color BORDER = new Color(226, 232, 237);
    public static final Color WHITE = Color.WHITE;

    private Theme() { }

    public static Font font(int size, int style) {
        return new Font("SansSerif", style, size);
    }
}
