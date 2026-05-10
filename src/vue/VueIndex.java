package vue;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import modele.Carte;
import modele.CarteDB;
import modele.CarteEffetDB;
/**
 * INDEX DES CARTES — encyclopédie scrollable
 *
 * Deux optimisations majeures pour la fluidité :
 *
 * 1. Préchargement (componentShown) :
 *    Au premier affichage, toutes les images sont chargées en RAM.
 *    Le scroll est instantané : Images.carte() = O(1) depuis le cache.
 *
 * 2. Cache de rendu (grilleCache) :
 *    La grille est rendue dans un BufferedImage une seule fois.
 *    Quand on scrolle → blit du cache (ultra-rapide).
 *    Quand on filtre → grilleCache = null → re-rendu au prochain paint.
 *    Sans ce cache, les 40 images étaient redessinées à chaque scroll.
 */
public class VueIndex extends Panel {
    private final Jeu       jeu;
    private List<Carte>     resultats  = new ArrayList<>();
    private boolean         precharge  = false; // préchargé une seule fois
    // Cache de rendu de la grille — invalidé à chaque filtrage
    private BufferedImage   grilleCache;
    private int             cacheCols, cacheRows; // dimensions au moment du rendu
    private final TextField champNom  = new TextField("", 12);
    private final Choice    choixElem = new Choice();
    private final TextField champHP   = new TextField("0", 3);
    private final TextField champATK  = new TextField("0", 3);
    private final Choice    choixTri  = new Choice();
    private final Choice    choixType = new Choice();

    private ScrollPane scroll;
    private Panel      grille;

    public VueIndex(Jeu j) {
        jeu = j;
        setLayout(new BorderLayout(4, 4));
        setBackground(Theme.BG_DARK);

        // Barre du haut
        Panel haut = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        haut.setBackground(Theme.BG_HEADER);
        haut.setPreferredSize(new Dimension(0, 50));
        BoutonImage retour = new BoutonImage("res/images/retour.png");
        retour.addActionListener(e -> j.allerA("accueil"));
        Label titre = new Label("INDEX DES CARTES");
        titre.setFont(Theme.FONT_TITLE);
        titre.setForeground(Theme.ACCENT_GOLD);
        haut.add(retour); haut.add(titre);

        // Grille scrollable avec cache de rendu
        grille = new Panel() {
            public void paint(Graphics g0) { peindreGrille(g0); }
            public void update(Graphics g) { paint(g); }
        };
        grille.setBackground(Theme.BG_DARK);

        scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scroll.add(grille);
        scroll.setBackground(Theme.BG_DARK);

        // Barre de filtres (en bas)
        Panel filtres = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filtres.setBackground(Theme.BG_HEADER);
        filtres.setPreferredSize(new Dimension(0, 46));

        choixType.add("Personnages"); choixType.add("Effets");
        choixElem.add("Tous");
        choixElem.add("Feu");    choixElem.add("Glace");
        choixElem.add("Foudre"); choixElem.add("Eau");
        choixElem.add("Vent");   choixElem.add("Pierre"); choixElem.add("Nature");
        choixTri.add("Aucun tri"); choixTri.add("HP desc");
        choixTri.add("ATK desc"); choixTri.add("Nom A-Z");

        Button chercher = new Button("Rechercher");
        chercher.setBackground(new Color(45, 75, 160));
        chercher.setForeground(Color.WHITE);
        chercher.setFont(Theme.FONT_BTN);
        chercher.addActionListener(e -> filtrer());

        filtres.add(lb("Type:"));  filtres.add(choixType);
        filtres.add(lb("Nom:"));   filtres.add(champNom);
        filtres.add(lb("Elem:"));  filtres.add(choixElem);
        filtres.add(lb("HP>="));   filtres.add(champHP);
        filtres.add(lb("ATK>=")); filtres.add(champATK);
        filtres.add(choixTri);
        filtres.add(chercher);

        add(haut,    BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(filtres, BorderLayout.SOUTH);

        resultats = CarteDB.toutes();
        majTailleGrille();

        // Préchargement au premier affichage + recalcul du scroll
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                if (!precharge) {
                    // Charge toutes les images en RAM une seule fois
                    Images.prechargerToutesCartes(CarteDB.taille());
                    precharge = true;
                }
                majTailleGrille();
                scroll.validate();
                scroll.doLayout();
                grille.repaint();
            }
        });
    }

    private void filtrer() {
        if (choixType.getSelectedIndex() == 1) {
            resultats.clear();
            invaliderCache();
            majTailleGrille();
            return;
        }
        String nom  = champNom.getText().trim();
        String elem = choixElem.getSelectedIndex() == 0 ? "" : choixElem.getSelectedItem();
        int minHP = 0, minATK = 0;
        try { minHP  = Integer.parseInt(champHP.getText().trim());  } catch (Exception ignored) {}
        try { minATK = Integer.parseInt(champATK.getText().trim()); } catch (Exception ignored) {}

        java.util.Comparator<Carte> comp = null;
        if (choixTri.getSelectedIndex() == 1) comp = CarteDB.PAR_HP_DESC;
        if (choixTri.getSelectedIndex() == 2) comp = CarteDB.PAR_ATK_DESC;
        if (choixTri.getSelectedIndex() == 3) comp = CarteDB.PAR_NOM_ASC;

        resultats = CarteDB.filtrer(nom, elem, minHP, minATK, comp);
        invaliderCache(); // les résultats ont changé → recalculer le rendu
        majTailleGrille();
    }

    /** Invalide le cache de rendu → sera recalculé au prochain paint(). */
    private void invaliderCache() {
        grilleCache = null;
    }

    private void majTailleGrille() {
        boolean effets = (choixType.getSelectedIndex() == 1);
        int n    = effets ? CarteEffetDB.nbEffets() : resultats.size();
        int cols = 5, cw = 102, ch = 172, gap = 12;
        int rows = Math.max(1, (n + cols - 1) / cols);
        grille.setPreferredSize(new Dimension(cols * (cw + gap) + gap + 30, rows * (ch + gap) + gap + 20));
        scroll.validate();
        scroll.doLayout();
        grille.repaint();
    }

    /**
     * Rendu de la grille :
     *   - Si grilleCache est null (premier affichage ou après filtrage) → re-rendu complet dans le cache
     *   - Sinon → simple blit du cache (instantané même avec 40 cartes)
     */
    private void peindreGrille(Graphics g0) {
        int W = grille.getWidth(), H = grille.getHeight();
        if (W <= 0 || H <= 0) return;

        if (grilleCache == null || grilleCache.getWidth() != W || grilleCache.getHeight() != H) {
            grilleCache = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = grilleCache.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            dessinerGrille(bg, W, H);
            bg.dispose();
        }

        g0.drawImage(grilleCache, 0, 0, grille); // blit = 1 opération, toujours rapide
    }

    private void dessinerGrille(Graphics2D g, int pw, int ph) {
        int cw = 102, ch = 172, gap = 12, cols = 5;

        g.setColor(Theme.BG_DARK); g.fillRect(0, 0, pw, ph);
        BufferedImage fond = Images.get("res/images/fond.png");
        if (fond != null) {
            g.drawImage(fond, 0, 0, pw, ph, null);
            g.setColor(new Color(8, 5, 18, 175)); g.fillRect(0, 0, pw, ph);
        }

        int startX = (pw - (cols * (cw + gap) - gap)) / 2;
        boolean effets = (choixType.getSelectedIndex() == 1);

        if (effets) {
            for (int i = 1; i <= CarteEffetDB.nbEffets(); i++) {
                int x = startX + ((i - 1) % cols) * (cw + gap);
                int y = gap    + ((i - 1) / cols) * (ch + gap);
                dessinerEffet(g, i, x, y, cw, ch);
            }
        } else {
            for (int i = 0; i < resultats.size(); i++) {
                int x = startX + (i % cols) * (cw + gap);
                int y = gap    + (i / cols) * (ch + gap);
                dessinerPerso(g, resultats.get(i), x, y, cw, ch);
            }
            if (resultats.isEmpty()) {
                g.setColor(Theme.TEXT_DIM); g.setFont(Theme.FONT_LABEL);
                g.drawString("Aucun resultat.", 20, 50);
            }
        }
    }

    private void dessinerPerso(Graphics2D g, Carte c, int x, int y, int cw, int ch) {
        BufferedImage img = Images.carte(c.getId());
        if (img != null) g.drawImage(img, x, y, cw, ch, null);
        else { g.setColor(Theme.BG_CARD); g.fillRect(x, y, cw, ch); }
        g.setColor(new Color(0, 0, 0, 210)); g.fillRect(x, y + ch - 56, cw, 56);
        g.setColor(Theme.TEXT_PRIMARY); g.setFont(Theme.FONT_CARD);
        g.drawString(c.getNom(), x + 4, y + ch - 42);
        g.setColor(Theme.couleurElement(c.getElement())); g.setFont(Theme.FONT_SMALL);
        g.drawString(c.getElement(), x + 4, y + ch - 30);
        g.setColor(Theme.TEXT_DIM);
        String stats = "HP " + c.getHpMax() + "  ATK " + c.getAttaque();
        if (c.getHeal() > 0) stats += "  HEAL " + c.getHeal();
        g.drawString(stats, x + 4, y + ch - 6);
        g.setColor(new Color(210, 168, 45, 80)); g.drawRect(x, y, cw - 1, ch - 1);
    }

    private void dessinerEffet(Graphics2D g, int id, int x, int y, int cw, int ch) {
        BufferedImage img = Images.effet(id);
        if (img != null) g.drawImage(img, x, y, cw, ch, null);
        else { g.setColor(new Color(28, 45, 20)); g.fillRect(x, y, cw, ch); }
        g.setColor(new Color(0, 0, 0, 210)); g.fillRect(x, y + ch - 50, cw, 50);
        g.setColor(Theme.ACCENT_GOLD); g.setFont(Theme.FONT_CARD);
        g.drawString(CarteEffetDB.getNom(id), x + 4, y + ch - 34);
        g.setColor(Theme.TEXT_DIM); g.setFont(Theme.FONT_SMALL);
        String desc = CarteEffetDB.getDesc(id);
        if (desc.length() > 20) {
            g.drawString(desc.substring(0, 20), x + 4, y + ch - 20);
            g.drawString(desc.substring(20),    x + 4, y + ch - 6);
        } else {
            g.drawString(desc, x + 4, y + ch - 10);
        }
    }

    private Label lb(String t) {
        Label l = new Label(t);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setFont(Theme.FONT_SMALL);
        return l;
    }
}

    // filtres et scroll
