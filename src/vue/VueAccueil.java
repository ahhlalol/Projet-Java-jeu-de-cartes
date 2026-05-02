package vue;
import java.awt.*;
import java.awt.image.BufferedImage;
import modele.Score;
import modele.ScoreManager;
/**
 * ÉCRAN D'ACCUEIL
 *
 * Le Canvas (CENTER) dessine en Graphics2D :
 *   1. fond.png plein écran, sans overlay
 *   2. titre.png centré
 *   3. Meilleur score en bas du canvas (dessiné directement, toujours visible)
 *
 * Le Panel bas (SOUTH, 110px) contient les boutons Jouer / Index,
 * positionnés plus haut qu'avant (FlowLayout avec petit vgap).
 */
public class VueAccueil extends Panel {
    private String texteScore = "";
    private Canvas canvas;
    public VueAccueil(Jeu j) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        // ── Canvas : fond + titre + score ─────────────────────────────────
        canvas = new Canvas() {
            public void paint(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                   RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Fond de secours
                g.setColor(Theme.BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());

                // fond.png plein écran — sans overlay noir par-dessus
                BufferedImage fond = Images.get("res/images/fond.png");
                if (fond != null)
                    g.drawImage(fond, 0, 0, getWidth(), getHeight(), this);

                // Logo / titre centré
                BufferedImage titre = Images.get("res/images/titre.png");
                if (titre != null) {
                    int s = 480;
                    int x = (getWidth() - s)  / 2;
                    int y = (getHeight() - s) / 2;
                    g.drawImage(titre, x, y, s, s, this);
                }

                // Meilleur score — 17pt Cinzel doré, centré, en bas du canvas
                // Dessiné en Graphics2D : toujours visible, aucun problème de rendu AWT
                if (!texteScore.isEmpty()) {
                    g.setFont(new Font(Theme.CINZEL, Font.BOLD, 17));
                    g.setColor(Theme.ACCENT_GOLD);
                    FontMetrics fm = g.getFontMetrics();
                    int tw = fm.stringWidth(texteScore);
                    g.drawString(texteScore, (getWidth() - tw) / 2, getHeight() - 18);
                }
            }
            public void update(Graphics g) { paint(g); }
        };
        canvas.setBackground(Theme.BG_DARK);

        // ── Boutons Jouer / Index ─────────────────────────────────────────
        // vgap=4 (petit) : les boutons sont collés en haut du panel bas → plus hauts à l'écran
        Panel boutons = new Panel(new FlowLayout(FlowLayout.CENTER, 55, 4));
        boutons.setBackground(new Color(8, 5, 18));

        BoutonImage jouer = new BoutonImage("res/images/bouton_jouer.png");
        jouer.addActionListener(e -> j.allerA("selection"));

        BoutonImage index = new BoutonImage("res/images/bouton_index.png");
        index.addActionListener(e -> j.allerA("index"));

        boutons.add(jouer);
        boutons.add(index);

        // Panel bas : 110px — moins haut qu'avant, boutons en NORTH → plus remontés
        Panel bas = new Panel(new BorderLayout());
        bas.setBackground(new Color(8, 5, 18));
        bas.setPreferredSize(new Dimension(0, 110));
        bas.add(boutons, BorderLayout.NORTH);

        add(canvas, BorderLayout.CENTER);
        add(bas,    BorderLayout.SOUTH);

        majScore();
    }

    /** Relit scores.dat et rafraîchit le texte dessiné dans le canvas. */
    public void majScore() {
        Score best = ScoreManager.charger();
        texteScore = (best != null)
            ? "MEILLEUR SCORE : " + best.getNbRounds() + " rounds   " + best.getDate()
            : "Aucun record — Jouez pour en etablir un !";
        if (canvas != null) canvas.repaint();
    }
}

    // fond png
