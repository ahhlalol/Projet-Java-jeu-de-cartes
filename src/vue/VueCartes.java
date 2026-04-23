package vue;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;
import modele.*;

/**
 * Affiche les 3 cartes d'un joueur.
 *
 * Double buffer RÉEL :
 *   On dessine dans un BufferedImage hors-écran, puis on blitte en 1 drawImage().
 *   Résultat : aucun artefact, aucun scintillement, même pendant les flashs.
 *   (L'astuce update→paint seule ne suffit pas : le dessin reste visible frame par frame.)
 *
 * Flash rouge/vert :
 *   Un Timer remet flashDmg[i]/flashSoin[i] à false après 350/500ms → repaint().
 */
public class VueCartes extends Panel implements Observer, MouseListener {

    private final Modele  modele;
    private final boolean estJ1;
    private static final int CW = 140, CH = 240, PAD = 20;

    private final boolean[] flashDmg  = new boolean[3];
    private final boolean[] flashSoin = new boolean[3];
    private final int[]     hpAvant   = new int[3];

    // Buffer hors-écran : toute la logique de dessin va dedans
    private BufferedImage buffer;

    public VueCartes(Modele m, boolean j1) {
        modele = m; estJ1 = j1;
        m.addObserver(this);
        setBackground(Theme.BG_DARK);
        setPreferredSize(new Dimension(3 * CW + 4 * PAD, CH + 2 * PAD));
        addMouseListener(this);
    }

    /**
     * paint() = créer/réutiliser le buffer, dessiner dedans, blitter en 1 coup.
     * Le buffer n'est recréé que si la taille du composant change (rare).
     */
    @Override
    public void paint(Graphics g0) {
        int W = getWidth(), H = getHeight();
        if (W <= 0 || H <= 0) return;

        if (buffer == null || buffer.getWidth() != W || buffer.getHeight() != H)
            buffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        dessiner(g);

        g.dispose();
        g0.drawImage(buffer, 0, 0, this); // blit unique → aucun artefact
    }

    /** Redirige repaint() vers paint() directement (évite l'effacement AWT). */
    @Override
    public void update(Graphics g) { paint(g); }

    /** Tout le rendu graphique — appelé sur le buffer hors-écran. */
    private void dessiner(Graphics2D g) {
        g.setColor(Theme.BG_DARK);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        Deck  deck  = estJ1 ? modele.getMainJ1()  : modele.getMainJ2();
        Carte perso = estJ1 ? modele.getPersoJ1() : modele.getPersoJ2();

        for (int i = 0; i < deck.getCartes().size(); i++) {
            Carte c = deck.getCartes().get(i);
            int x = PAD + i * (CW + PAD), y = PAD;
            boolean actif = (c == perso);

            // Bordure dorée pour le personnage actif
            if (actif) {
                g.setColor(Theme.BORDER_ACTIVE);
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(x - 4, y - 4, CW + 8, CH + 8, 10, 10);
                g.setStroke(new BasicStroke(1));
            }

            // Image de la carte (déjà en cache RAM après prechargeImages())
            BufferedImage img = Images.carte(c.getId());
            if (img != null) g.drawImage(img, x, y, CW, CH, null);
            else { g.setColor(actif ? new Color(70, 50, 80) : Theme.BG_CARD); g.fillRect(x, y, CW, CH); }

            // Flash rouge (dégâts)
            if (i < 3 && flashDmg[i]) {
                g.setColor(new Color(255, 30, 30, 130)); g.fillRect(x, y, CW, CH);
                g.setColor(new Color(255, 60, 60));
                g.setStroke(new BasicStroke(3)); g.drawRect(x, y, CW, CH); g.setStroke(new BasicStroke(1));
            }
            // Flash vert (soin)
            if (i < 3 && flashSoin[i]) {
                g.setColor(new Color(40, 220, 80, 110)); g.fillRect(x, y, CW, CH);
                g.setColor(Theme.ACCENT_GREEN);
                g.setStroke(new BasicStroke(3)); g.drawRect(x, y, CW, CH); g.setStroke(new BasicStroke(1));
                g.setFont(new Font("Arial", Font.BOLD, 38));
                g.setColor(new Color(60, 255, 100));
                g.drawString("+", x + CW / 2 - 11, y + CH / 2 + 13);
            }

            // Bandeau stats en bas
            g.setColor(new Color(4, 2, 14, 220)); g.fillRect(x, y + CH - 90, CW, 90);

            // Barre HP colorée selon le niveau de vie
            g.setColor(new Color(60, 0, 0)); g.fillRect(x + 5, y + CH - 85, CW - 10, 9);
            if (c.getHpMax() > 0) {
                float r = (float) c.getHp() / c.getHpMax();
                g.setColor(r > 0.6f ? new Color(45, 185, 50)
                         : r > 0.3f ? new Color(210, 170, 25)
                                    : new Color(210, 50, 25));
                g.fillRect(x + 5, y + CH - 85, Math.max(0, (int)(r * (CW - 10))), 9);
            }

            g.setColor(Theme.TEXT_PRIMARY); g.setFont(Theme.FONT_CARD);
            g.drawString(c.getNom(), x + 5, y + CH - 71);
            g.setFont(Theme.FONT_SMALL); g.setColor(Theme.TEXT_DIM);
            g.drawString(c.getHp() + "/" + c.getHpMax() + " HP", x + 5, y + CH - 57);
            g.setColor(Theme.ACCENT_RED);
            g.drawString("ATK " + c.getAttaque(), x + 5, y + CH - 42);
            if (c.getHeal() > 0) { g.setColor(Theme.ACCENT_GREEN); g.drawString("HEAL " + c.getHeal(), x + 72, y + CH - 42); }
            g.setColor(Theme.couleurElement(c.getElement())); g.setFont(Theme.FONT_ITALIC);
            g.drawString(c.getElement(), x + 5, y + CH - 28);

            // Overlay KO
            if (c.isEnsanglantee()) {
                g.setColor(new Color(120, 0, 0, 140)); g.fillRect(x, y, CW, CH);
                g.setColor(new Color(255, 55, 55)); g.setFont(Theme.FONT_ROUND);
                g.drawString("KO", x + CW / 2 - 22, y + CH / 2 + 10);
            }
        }
    }

    /** Observer : détecte les changements de HP → déclenche les flashs. */
    @Override
    public void update(Observable o, Object arg) {
        Deck deck = estJ1 ? modele.getMainJ1() : modele.getMainJ2();
        for (int i = 0; i < Math.min(deck.getCartes().size(), 3); i++) {
            int hpNow = deck.getCartes().get(i).getHp();
            final int idx = i;
            if (hpNow < hpAvant[i]) {
                flashDmg[idx] = true;
                new Timer(350, e -> { flashDmg[idx] = false; repaint(); ((Timer)e.getSource()).stop(); }).start();
            } else if (hpNow > hpAvant[i]) {
                flashSoin[idx] = true;
                new Timer(500, e -> { flashSoin[idx] = false; repaint(); ((Timer)e.getSource()).stop(); }).start();
            }
            hpAvant[i] = hpNow;
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!estJ1) return;
        int index = (e.getX() - PAD) / (CW + PAD);
        if (index < 0 || index >= 3) return;
        if (modele.getEtat() == Modele.Etat.CHOISIR && modele.isJ1joue()) modele.choisirPerso(index);
        else if (modele.getEtat() == Modele.Etat.ACTION  && modele.isJ1joue()) modele.healer(index);
    }
    public void mousePressed(MouseEvent e)  {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
}
