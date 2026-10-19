import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Draws the 512x512 Workshop preview: a mock of the Mod Settings page. Usage: java MakePreview out.png */
public class MakePreview {

    static final Color PANEL = new Color(0x25, 0x33, 0x40);
    static final Color EDGE = new Color(0xb9, 0xc0, 0xc6);
    static final Color TEXT = new Color(0xd6, 0xda, 0xdd);
    static final Color WIDGET = new Color(0x3a, 0x47, 0x52);

    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background in the game's water teal, with a soft diagonal pattern
        g.setPaint(new GradientPaint(0, 0, new Color(0x1d, 0x7c, 0x9c), 512, 512, new Color(0x15, 0x5d, 0x7d)));
        g.fillRect(0, 0, 512, 512);
        g.setColor(new Color(255, 255, 255, 12));
        for (int i = -512; i < 512; i += 32) {
            g.fillPolygon(new int[] { i, i + 16, i + 528, i + 512 }, new int[] { 0, 0, 512, 512 }, 4);
        }

        int x = 56, y = 40, w = 400, h = 432;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(x + 6, y + 8, w, h);
        g.setColor(PANEL);
        g.fillRect(x, y, w, h);
        g.setColor(EDGE);
        g.setStroke(new BasicStroke(3));
        g.drawRect(x, y, w, h);

        centered(g, "Mod Settings", 24, x + w / 2, y + 42);
        left(g, "SettingsForge", 22, x + 22, y + 88);
        left(g, "Demo", 17, x + 22, y + 122);

        // checkbox
        g.setColor(WIDGET);
        g.fillRect(x + 26, y + 138, 22, 22);
        g.setColor(EDGE);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x + 26, y + 138, 22, 22);
        g.setStroke(new BasicStroke(3));
        g.drawLine(x + 31, y + 150, x + 36, y + 156);
        g.drawLine(x + 36, y + 156, x + 44, y + 143);
        left(g, "Demo checkbox", 15, x + 58, y + 155);

        // slider
        left(g, "Demo slider", 17, x + 22, y + 196);
        right(g, "50", 17, x + w - 22, y + 196);
        g.setColor(WIDGET);
        g.fillRect(x + 22, y + 214, w - 44, 8);
        g.setColor(EDGE);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x + 22, y + 214, w - 44, 8);
        g.setColor(TEXT);
        g.fillOval(x + w / 2 - 11, y + 207, 22, 22);

        // dropdown
        left(g, "Demo dropdown", 17, x + 22, y + 262);
        g.setColor(WIDGET);
        g.fillRect(x + 22, y + 274, w - 44, 32);
        g.setColor(EDGE);
        g.drawRect(x + 22, y + 274, w - 44, 32);
        centered(g, "Medium", 17, x + w / 2, y + 296);

        // second mod
        left(g, "Your Mod", 22, x + 22, y + 348);
        g.setColor(WIDGET);
        g.fillRect(x + 26, y + 360, 22, 22);
        g.setColor(EDGE);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x + 26, y + 360, 22, 22);
        left(g, "Your setting", 15, x + 58, y + 377);

        // buttons
        for (int i = 0; i < 2; i++) {
            int bx = x + 8 + i * (w / 2 - 4);
            g.setColor(WIDGET);
            g.fillRect(bx, y + h - 36, w / 2 - 12, 28);
            g.setColor(EDGE);
            g.drawRect(bx, y + h - 36, w / 2 - 12, 28);
            centered(g, i == 0 ? "Save" : "Cancel", 16, bx + (w / 2 - 12) / 2, y + h - 16);
        }

        g.dispose();
        ImageIO.write(img, "png", new File(args[0]));
    }

    static void left(Graphics2D g, String s, int size, int x, int y) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.setColor(TEXT);
        g.drawString(s, x, y);
    }

    static void right(Graphics2D g, String s, int size, int x, int y) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.setColor(TEXT);
        g.drawString(s, x - g.getFontMetrics().stringWidth(s), y);
    }

    static void centered(Graphics2D g, String s, int size, int x, int y) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.setColor(TEXT);
        g.drawString(s, x - g.getFontMetrics().stringWidth(s) / 2, y);
    }
}
