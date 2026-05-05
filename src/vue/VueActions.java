package vue;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import modele.Modele;
/**
 * BOUTONS D'ACTION DU JOUEUR
 *
 * Contient uniquement les 5 boutons de jeu + bouton PAUSE.
 * Les instructions et messages sont maintenant dans le bandeau SOUTH de VuePlateau.
 *
 * Si res/images/bg_gauche.png existe, il s'affiche en fond derrière les boutons.
 * Les sous-panels sont "transparents" (ils n'effacent pas le fond)
 * pour laisser l'image transparaître.
 */
public class VueActions extends Panel implements Observer {
    private final Modele modele;
    private final Jeu    jeu;
    private BufferedImage bgImage;

    public VueActions(Modele m, ActionListener ctrl, Jeu j) {
        modele = m; jeu = j;
        m.addObserver(this);

        // Charger l'image de fond si elle existe (optionnelle)
        try {
            File f = new File("res/images/bg_gauche.png");
            if (f.exists()) bgImage = ImageIO.read(f);
        } catch (Exception ignored) {}

        setLayout(new BorderLayout(0, 8));

        // Bouton PAUSE en haut à droite
        Button btnPause = new Button("PAUSE");
        btnPause.setBackground(new Color(38, 36, 58));
        btnPause.setForeground(new Color(190, 185, 220));
        btnPause.setFont(Theme.FONT_BTN);
        btnPause.addActionListener(e -> afficherPause());

        Panel topBar = transparent(new FlowLayout(FlowLayout.RIGHT, 5, 3));
        topBar.add(btnPause);

        // Les 5 boutons d'action (noms utilisés par Controleur.actionPerformed)
        Panel boutons = transparent(new GridLayout(5, 1, 0, 12));
        boutons.add(btn("PIOCHER ENERGIES",      "LANCER", new Color(25, 85, 40),   ctrl));
        boutons.add(btn("ATTAQUER (1 energie)",  "ATK",    new Color(165, 60, 8),   ctrl));
        boutons.add(btn("SUPER ATK (2 energies)","SUPER",  new Color(140, 12, 12),  ctrl));
        boutons.add(btn("SOIN (3 energies)",     "SOIN",   new Color(22, 80, 120),  ctrl));
        boutons.add(btn("FIN DE TOUR",           "ROUND",  new Color(90, 55, 100),  ctrl));

        Panel wrap = transparent(new FlowLayout(FlowLayout.CENTER, 0, 18));
        boutons.setPreferredSize(new Dimension(188, 235));
        wrap.add(boutons);

        add(topBar, BorderLayout.NORTH);
        add(wrap,   BorderLayout.CENTER);
        // Pas de lblGuide ni de hint ici — ils sont maintenant dans BandeauInstructions (SOUTH de VuePlateau)
    }

    /** Dessine bg_gauche.png en fond, ou le fond sombre par défaut. */
    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(10, 8, 20, 145));
            g.fillRect(0, 0, getWidth(), getHeight()); // assombrir pour lisibilité
        } else {
            g.setColor(Theme.BG_PANEL);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        paintComponents(g); // dessiner les boutons par-dessus
    }
    public void update(Graphics g) { paint(g); }

    private void afficherPause() {
        Object[] opts = {"Continuer", "Retour a l'accueil"};
        int choix = JOptionPane.showOptionDialog(this, "La partie est en pause.", "PAUSE",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        if (choix == 1) jeu.allerA("accueil");
    }

    /** Crée un bouton d'action stylisé et l'associe au Controleur via son nom. */
    private Button btn(String label, String name, Color bg, ActionListener ctrl) {
        Button b = new Button(label);
        b.setName(name); // le Controleur lit ce nom dans actionPerformed()
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.FONT_BTN);
        b.addActionListener(ctrl);
        return b;
    }

    /**
     * Panel "transparent" : ne remplit pas son fond → laisse le fond du parent apparaître.
     * Nécessaire pour que bg_gauche.png reste visible derrière les boutons.
     */
    private static Panel transparent(LayoutManager lm) {
        return new Panel(lm) {
            public void paint(Graphics g)  { paintComponents(g); }
            public void update(Graphics g) { paint(g); }
        };
    }

    // Observer : rafraîchit uniquement si l'état visuel a changé
    public void update(Observable o, Object arg) { repaint(); }
}

    // pause
