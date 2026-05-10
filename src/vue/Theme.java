package vue;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
/**
 * Constantes visuelles : palette, polices — source unique de verite.
 * Police Cinzel : placer Cinzel-Regular.ttf dans res/fonts/ (gratuit sur fonts.google.com).
 */
public final class Theme {
    private Theme() {}
    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(12, 10, 25);
    public static final Color BG_PANEL      = new Color(20, 18, 35);
    public static final Color BG_HEADER     = new Color(22, 20, 42);
    public static final Color BG_CARD       = new Color(28, 25, 48);
    public static final Color ACCENT_GOLD   = new Color(210, 168, 45);
    public static final Color ACCENT_BLUE   = new Color(70, 130, 220);
    public static final Color ACCENT_RED    = new Color(200, 50, 50);
    public static final Color ACCENT_GREEN  = new Color(50, 200, 90);
    public static final Color TEXT_PRIMARY  = new Color(230, 225, 210);
    public static final Color TEXT_DIM      = new Color(130, 125, 110);
    public static final Color BORDER_ACTIVE = new Color(230, 200, 60);

    // ── Chargement Cinzel ────────────────────────────────────────────────────
    public static final String CINZEL;
    static {
        String fam = "Georgia"; // fallback si Cinzel absent
        try {
            File f = new File("res/fonts/Cinzel-Regular.ttf");
            if (f.exists()) {
                Font cin = Font.createFont(Font.TRUETYPE_FONT, f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(cin);
                fam = cin.getFamily();
            }
        } catch (FontFormatException | IOException ignored) {}
        CINZEL = fam;
    }

    // ── Polices ──────────────────────────────────────────────────────────────
    public static final Font FONT_HUGE  = new Font(CINZEL, Font.PLAIN, 38);
    public static final Font FONT_ROUND = new Font(CINZEL, Font.PLAIN, 24);
    public static final Font FONT_TITLE = new Font(CINZEL, Font.PLAIN, 18);
    public static final Font FONT_LABEL = new Font(CINZEL, Font.PLAIN, 13);
    public static final Font FONT_CARD  = new Font(CINZEL, Font.PLAIN, 11);
    public static final Font FONT_BTN   = new Font(CINZEL, Font.PLAIN, 11);
    public static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 10);
    public static final Font FONT_ITALIC= new Font("Arial", Font.ITALIC, 10);

    // ── Couleur par element ──────────────────────────────────────────────────
    public static Color couleurElement(String e) {
        switch (e) {
            case "Feu":    return new Color(255, 110, 40);
            case "Glace":  return new Color(130, 210, 255);
            case "Foudre": return new Color(180, 90,  255);
            case "Eau":    return new Color(60,  150, 255);
            case "Vent":   return new Color(60,  215, 160);
            case "Pierre": return new Color(200, 165, 55);
            case "Nature": return new Color(60,  195, 75);
            default:       return TEXT_PRIMARY;
        }
    }
}

    // polices couleurs

    // final
