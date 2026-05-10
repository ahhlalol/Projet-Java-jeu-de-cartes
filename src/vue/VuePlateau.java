package vue;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import controleur.Controleur;
import controleur.IA;
import modele.Modele;
/**
 * PLATEAU DE JEU PRINCIPAL
 *
 * Mise en page (BorderLayout avec gaps de 3px dorés) :
 *
 *   ┌──────────┬──────────────────────┬──────────┐
 *   │  WEST    │  CENTER              │  EAST    │
 *   │ (222px)  │  rangée ennemi       │ (162px)  │
 *   │          │  VueAnimation        │ effets   │
 *   │ message  │  rangée joueur       │          │
 *   │ actions  │                      │          │
 *   ├──────────┴──────────────────────┴──────────┤
 *   │  SOUTH : bandeau instructions (40px)        │
 *   └────────────────────────────────────────────┘
 *
 * Le SOUTH affiche le dernier message du jeu ("Votre tour !", "Pas assez de dés !"…)
 * et le hint "clic sur une carte = soin ciblé".
 * setBackground(ACCENT_GOLD) + BorderLayout(3,3) → séparateurs dorés entre les zones.
 */
public class VuePlateau extends Panel {
    public VuePlateau(Modele m, Controleur c, Jeu j) {
        setBackground(Theme.ACCENT_GOLD); // les gaps 3px apparaissent en doré
        setLayout(new BorderLayout(3, 3));
        // L'IA s'abonne au Modele ici — elle jouera seule quand c'est son tour
        new IA(m);
        // ── Zone centrale : rangées de cartes + animation GIF ──────────────
        Panel centre = new Panel(new BorderLayout(0, 3));
        centre.setBackground(Theme.BG_DARK);
        centre.add(rangee(m, false),    BorderLayout.NORTH);  // cartes IA
        centre.add(new VueAnimation(m), BorderLayout.CENTER); // animation centrale
        centre.add(rangee(m, true),     BorderLayout.SOUTH);  // cartes joueur
        add(centre, BorderLayout.CENTER);
        // ── Panneau gauche : round + boutons d'action ──────────────────────
        Panel gauche = new Panel(new BorderLayout(0, 3));
        gauche.setBackground(Theme.BG_DARK);
        gauche.setPreferredSize(new Dimension(222, 0));
        gauche.add(new VueMessage(m), BorderLayout.NORTH);
        // CardLayout : bascule entre les boutons de jeu et l'écran de fin
        CardLayout cl = new CardLayout();
        Panel switchPanel = new Panel(cl);
        switchPanel.add(new VueActions(m, c, j), "jeu");
        switchPanel.add(new VueGagnant(m, cl, switchPanel, j), "fin");
        cl.show(switchPanel, "jeu");
        gauche.add(switchPanel, BorderLayout.CENTER);
        add(gauche, BorderLayout.WEST);
        // ── Panneau droit : effets en main ─────────────────────────────────
        Panel droite = new Panel(new GridLayout(2, 1, 0, 3));
        droite.setBackground(Theme.BG_DARK);
        droite.setPreferredSize(new Dimension(162, 0));
        droite.add(new VuePioche(m, false)); // effet IA (en haut)
        droite.add(new VuePioche(m, true));  // effet joueur (en bas)
        add(droite, BorderLayout.EAST);
        // ── Bandeau instructions en bas (pleine largeur) ───────────────────
        // Affiche le message courant du jeu + le hint "clic carte = soin"
        add(new BandeauInstructions(m), BorderLayout.SOUTH);
    }
    /** Crée une rangée = dés (gauche) + 3 cartes (centre) pour un joueur. */
    private Panel rangee(Modele m, boolean j1) {
        Panel p = new Panel(new BorderLayout(3, 0));
        p.setBackground(Theme.BG_DARK);
        p.add(new VueDes(m, j1),    BorderLayout.WEST);
        p.add(new VueCartes(m, j1), BorderLayout.CENTER);
        return p;
    }

    // ── Bandeau d'instructions ─────────────────────────────────────────────

    /**
     * Bandeau pleine largeur en bas de l'écran.
     * Affiche en temps réel : message du jeu à gauche, hint à droite.
     *
     * Observe le Modele directement → se rafraîchit automatiquement
     * à chaque changement d'état (attaque, soin, tour…).
     */
    static class BandeauInstructions extends Panel implements Observer {

        private final Modele modele;

        BandeauInstructions(Modele m) {
            modele = m;
            m.addObserver(this);
            setBackground(new Color(14, 12, 28));
            setPreferredSize(new Dimension(0, 38));
        }

        public void update(Graphics g) { paint(g); }

        public void paint(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond avec une fine ligne dorée en haut (séparateur visuel)
            g.setColor(new Color(14, 12, 28));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(210, 168, 45, 80));
            g.fillRect(0, 0, getWidth(), 2);

            // Message courant (ex : "Votre tour !", "Pas assez de dés !")
            String msg = modele.getMessage();
            if (msg != null && !msg.isEmpty()) {
                g.setFont(Theme.FONT_LABEL);
                g.setColor(Theme.TEXT_PRIMARY);
                g.drawString(msg, 14, 25);
            }

            // Hint à droite : rappel de l'action de clic sur une carte
            String hint = "clic sur une carte = soin cible  |  3 energies";
            g.setFont(Theme.FONT_ITALIC);
            g.setColor(new Color(80, 120, 170));
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, getWidth() - hw - 14, 25);
        }

        public void update(Observable o, Object arg) { repaint(); }
    }
}

    // layout

    // double buffer

    // fix deck vide
