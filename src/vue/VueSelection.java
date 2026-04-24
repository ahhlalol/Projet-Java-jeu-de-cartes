package vue;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import modele.Carte;
import modele.CarteDB;

/**
 * Écran de sélection des 3 personnages.
 * Affiche 20 cartes au hasard (5 cols × 4 lignes).
 *
 * Double buffer sur la grille :
 *   La grille est rendue dans un BufferedImage hors-écran, puis blittée.
 *   Résultat : défilement fluide même avec 20 images.
 *
 * Préchargement :
 *   melangerAffichees() appelle Images.prechargerCartes() pour les 20 cartes
 *   affichées → aucun accès disque pendant dessiner().
 */
public class VueSelection extends Panel {

    private static final int CW = 108, CH = 185, COLS = 5, ROWS = 4, GAP = 15;
    private static final int NB_AFFICHES = COLS * ROWS; // 20

    private final Jeu           jeu;
    private final List<Integer> affichees = new ArrayList<>();
    private final List<Integer> choix     = new ArrayList<>();

    private Label       lblInfo;
    private BoutonImage suivant;
    private Panel       grille;

    // Buffer hors-écran pour la grille
    private BufferedImage grilleBuffer;

    public VueSelection(Jeu j) {
        jeu = j;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_DARK);
        melangerAffichees();

        // En-tête
        Panel header = new Panel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        header.setBackground(Theme.BG_HEADER);
        BoutonImage retour = new BoutonImage("res/images/retour.png");
        retour.addActionListener(e -> j.allerA("accueil"));
        lblInfo = new Label("Choisissez vos 3 personnages  (0/3)", Label.CENTER);
        lblInfo.setFont(Theme.FONT_LABEL);
        lblInfo.setForeground(Theme.ACCENT_GOLD);
        suivant = new BoutonImage("res/images/suivant.png");
        suivant.addActionListener(e -> confirmer());
        suivant.setActif(false);
        header.add(retour); header.add(lblInfo); header.add(suivant);

        // Grille avec double buffer réel (BufferedImage)
        grille = new Panel() {
            public void paint(Graphics g0) { peindreGrille(g0); }
            public void update(Graphics g) { paint(g); }
        };
        grille.setBackground(Theme.BG_DARK);
        grille.setPreferredSize(new Dimension(COLS * (CW + GAP) + GAP + 80, ROWS * (CH + GAP) + GAP + 50));
        grille.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { clic(e.getX(), e.getY()); }
        });

        add(header, BorderLayout.NORTH);
        add(grille, BorderLayout.CENTER);
    }

    /**
     * Tire 20 cartes aléatoires et précharge leurs images.
     * Appelé au démarrage et après chaque partie.
     */
    private void melangerAffichees() {
        affichees.clear();
        grilleBuffer = null; // invalider le buffer : les cartes ont changé

        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < CarteDB.toutes().size(); i++) pool.add(i);
        Collections.shuffle(pool);
        for (int i = 0; i < Math.min(NB_AFFICHES, pool.size()); i++) affichees.add(pool.get(i));

        // Précharger les images des 20 cartes affichées + le dos
        List<Carte> toutes = CarteDB.toutes();
        List<Integer> ids  = new ArrayList<>();
        for (int idx : affichees) ids.add(toutes.get(idx).getId());
        ids.add(0); // dos de carte
        Images.prechargerCartes(ids);
    }

    private int positionSous(int mx, int my) {
        int pw = grille.getWidth();
        int debutX = (pw - (COLS * (CW + GAP) + GAP)) / 2;
        int relX = mx - debutX, relY = my - 12;
        if (relX < 0 || relY < 0) return -1;
        int col = relX / (CW + GAP), row = relY / (CH + GAP);
        if (relX % (CW + GAP) >= CW || relY % (CH + GAP) >= CH || col >= COLS || row >= ROWS) return -1;
        int pos = row * COLS + col;
        return pos < affichees.size() ? pos : -1;
    }

    private void clic(int mx, int my) {
        int pos = positionSous(mx, my);
        if (pos < 0) return;
        if (choix.contains(pos)) {
            choix.remove(Integer.valueOf(pos));
            lblInfo.setText("Choisissez vos 3 personnages  (" + choix.size() + "/3)");
            lblInfo.setForeground(Theme.ACCENT_GOLD);
            suivant.setActif(false);
        } else if (choix.size() < 3) {
            choix.add(pos);
            if (choix.size() == 3) {
                lblInfo.setText("L'adversaire choisit...");
                lblInfo.setForeground(Theme.ACCENT_GREEN);
                suivant.setActif(true);
                grille.repaint();
                new Timer(400, e -> { confirmer(); ((Timer)e.getSource()).stop(); }).start();
                return;
            }
            lblInfo.setText("Choisissez vos 3 personnages  (" + choix.size() + "/3)");
        }
        grille.repaint();
    }

    private void confirmer() {
        List<Carte> toutes = CarteDB.toutes();
        List<Carte> deckJ1 = new ArrayList<>();
        for (int pos : choix)
            deckJ1.add(CarteDB.getCarte(toutes.get(affichees.get(pos)).getId()));

        List<Carte> pool = CarteDB.toutes();
        Collections.shuffle(pool);
        List<Carte> deckJ2 = new ArrayList<>(pool.subList(0, 3));

        choix.clear(); melangerAffichees();
        lblInfo.setText("Choisissez vos 3 personnages  (0/3)");
        lblInfo.setForeground(Theme.ACCENT_GOLD);
        suivant.setActif(false);
        jeu.demarrerPartie(deckJ1, deckJ2);
    }

    /**
     * Rendu dans un BufferedImage hors-écran, puis blit.
     * grilleBuffer est recréé si la taille change (rare).
     */
    private void peindreGrille(Graphics g0) {
        int W = grille.getWidth(), H = grille.getHeight();
        if (W <= 0 || H <= 0) return;

        if (grilleBuffer == null || grilleBuffer.getWidth() != W || grilleBuffer.getHeight() != H)
            grilleBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = grilleBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        dessiner(g);

        g.dispose();
        g0.drawImage(grilleBuffer, 0, 0, grille); // blit unique
    }

    private void dessiner(Graphics2D g) {
        int pw = grille.getWidth();
        int debutX = (pw - (COLS * (CW + GAP) + GAP)) / 2, debutY = 12;

        g.setColor(Theme.BG_DARK); g.fillRect(0, 0, grille.getWidth(), grille.getHeight());

        List<Carte> toutes = CarteDB.toutes();
        BufferedImage dos   = Images.carte(0);

        for (int pos = 0; pos < affichees.size(); pos++) {
            Carte c  = toutes.get(affichees.get(pos));
            int x = debutX + (pos % COLS) * (CW + GAP);
            int y = debutY + (pos / COLS) * (CH + GAP);
            boolean sel = choix.contains(pos);

            if (sel) {
                g.setColor(new Color(210, 168, 45, 80));
                g.fillRoundRect(x - 5, y - 5, CW + 10, CH + 10, 14, 14);
                g.setColor(Theme.ACCENT_GOLD);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(x - 3, y - 3, CW + 6, CH + 6, 12, 12);
                g.setStroke(new BasicStroke(1));
                BufferedImage face = Images.carte(c.getId());
                if (face != null) g.drawImage(face, x, y, CW, CH, null);
                else { g.setColor(Theme.BG_CARD); g.fillRect(x, y, CW, CH); }
                g.setColor(Theme.ACCENT_GOLD); g.setFont(Theme.FONT_CARD);
                int tw = g.getFontMetrics().stringWidth(c.getNom());
                g.drawString(c.getNom(), x + CW / 2 - tw / 2, y + CH + 13);
            } else {
                if (dos != null) g.drawImage(dos, x, y, CW, CH, null);
                else { g.setColor(Theme.BG_CARD); g.fillRect(x, y, CW, CH); }
            }
        }

        // Barre de progression en bas
        int totalW = COLS * (CW + GAP) - GAP, barY = debutY + ROWS * (CH + GAP) + 10;
        g.setColor(new Color(40, 38, 60)); g.fillRoundRect(debutX, barY, totalW, 8, 6, 6);
        if (!choix.isEmpty()) {
            g.setColor(Theme.ACCENT_GREEN);
            g.fillRoundRect(debutX, barY, totalW * choix.size() / 3, 8, 6, 6);
        }
    }
}
