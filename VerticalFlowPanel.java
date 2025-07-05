// File: src/VerticalFlowPanel.java
import javax.swing.*;
import java.awt.*;

/**
 * Sebuah JPanel khusus yang menggunakan WrapLayout dan memaksa JScrollPane
 * untuk hanya melakukan scroll secara vertikal.
 * Panel ini akan selalu menyesuaikan lebarnya dengan area pandang (viewport)
 * dari JScrollPane, sehingga tidak akan pernah ada scrollbar horizontal.
 */
public class VerticalFlowPanel extends JPanel implements Scrollable {

    public VerticalFlowPanel() {
        // Menggunakan WrapLayout yang sudah kita punya untuk menata komponen
        setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
    }

    /**
     * Ini adalah method kunci. Dengan mengembalikannya sebagai 'true',
     * kita memberitahu JScrollPane untuk MENGATUR LEBAR panel ini,
     * bukan sebaliknya.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * Method ini memberitahu JScrollPane untuk TIDAK mengatur tinggi panel ini.
     * Biarkan tinggi panel tumbuh seiring dengan bertambahnya komponen.
     * Inilah yang membuat scrollbar vertikal muncul saat dibutuhkan.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        // Kecepatan scroll per putaran roda mouse
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        // Kecepatan scroll per klik pada background scrollbar
        return 16;
    }
}