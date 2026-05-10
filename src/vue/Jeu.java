package vue;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import controleur.Controleur;
import modele.Carte;
import modele.CarteDB;
import modele.Modele;
import modele.Son;

/**
 * FENÊTRE PRINCIPALE — point d'entrée (main)
 *
 * Navigation : CardLayout avec 5 écrans ("accueil", "index", "selection", "jeu", "chargement").
 * allerA() affiche "chargement" 600ms puis bascule → transition fluide.
 *
 * Préchargement des images au bon moment :
 *   - demarrerPartie() : précharge les 6 cartes du match (3 J1 + 3 J2 + dos)
 *     → aucun accès disque pendant VueCartes.paint()
 *   - Les images de l'Index (40 cartes) sont préchargées par VueIndex.componentShown()
 */
public class Jeu extends Frame implements WindowListener {

    private final CardLayout ecrans = new CardLayout();
    private final Panel      root   = new Panel(ecrans);
    private VueAccueil vueAccueil;
    private VuePlateau plateau;
    private final Image gifChargement =
        Toolkit.getDefaultToolkit().getImage("res/gifs/chargement.gif");

    public static void main(String[] args) { new Jeu(); }

    public Jeu() {
        Son.prechargerTout();
        add(root);
        addWindowListener(this);
        setTitle("Jeu de Cartes");
        setSize(1300, 1000);
        setLocationRelativeTo(null);

        vueAccueil = new VueAccueil(this);
        root.add(vueAccueil,             "accueil");
        root.add(new VueIndex(this),     "index");
        root.add(new VueSelection(this), "selection");
        root.add(new Panel(),            "jeu");
        root.add(panelChargement(),      "chargement");

        ecrans.show(root, "accueil");
        setVisible(true);
    }

    private Panel panelChargement() {
        Panel p = new Panel() {
            public void paint(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                int w = 450, h = 450, x = (getWidth() - w) / 2, y = (getHeight() - h) / 2 - 20;
                g.drawImage(gifChargement, x, y, w, h, this);
                g.setColor(Theme.TEXT_DIM);
                g.setFont(Theme.FONT_TITLE);
                g.drawString("Chargement...", x + w / 2 - 70, y + h + 35);
            }
            public void update(Graphics g) { paint(g); }
        };
        p.setBackground(Color.BLACK);
        return p;
    }

    /** Navigation avec transition chargement → écran cible. */
    public void allerA(String nom) {
        ecrans.show(root, "chargement");
        root.repaint();
        new Timer(600, e -> {
            ecrans.show(root, nom);
            root.validate();
            if ("accueil".equals(nom) && vueAccueil != null) vueAccueil.majScore();
            ((Timer) e.getSource()).stop();
        }).start();
    }

    /**
     * Lance une nouvelle partie.
     * Précharge les images des 6 cartes (J1 + J2) + le dos de carte
     * pour que VueCartes.paint() n'ait aucun accès disque pendant le jeu.
     */
    public void demarrerPartie(List<Carte> deckJ1, List<Carte> deckJ2) {
        if (plateau != null) root.remove(plateau);

        // Précharger les images AVANT de créer VuePlateau
        List<Integer> ids = new ArrayList<>();
        for (Carte c : deckJ1) ids.add(c.getId());
        for (Carte c : deckJ2) ids.add(c.getId());
        ids.add(0); // dos de carte (id 0)
        Images.prechargerCartes(ids);

        Modele m = new Modele(deckJ1, deckJ2);
        plateau  = new VuePlateau(m, new Controleur(m), this);
        root.add(plateau, "jeu");
        allerA("jeu");
    }

    public void windowClosing(WindowEvent e)     { System.exit(0); }
    public void windowOpened(WindowEvent e)      {}
    public void windowClosed(WindowEvent e)      {}
    public void windowIconified(WindowEvent e)   {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e)   {}
    public void windowDeactivated(WindowEvent e) {}
}
