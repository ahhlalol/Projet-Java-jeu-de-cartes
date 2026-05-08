package vue;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import modele.Modele;
import modele.Score;
import modele.ScoreManager;
import modele.Son;
/** Ecran de fin de partie : VICTOIRE ou DEFAITE. */
public class VueGagnant extends Panel implements Observer {

    private final Modele m; private final CardLayout card; private final Panel parent; private final Jeu jeu;
    private final Label lblResultat = new Label("", Label.CENTER);
    private final Label lblRounds   = new Label("", Label.CENTER);
    private final Label lblMeilleur = new Label("", Label.CENTER);
    private boolean scoreEnregistre = false;

    public VueGagnant(Modele mo, CardLayout c, Panel p, Jeu j) {
        m=mo; card=c; parent=p; jeu=j; mo.addObserver(this);
        Color bg = new Color(9,5,18); setBackground(bg); setLayout(new GridLayout(6,1,0,12));
        lblResultat.setFont(Theme.FONT_HUGE); lblResultat.setBackground(bg);
        lblRounds.setFont(Theme.FONT_LABEL); lblRounds.setForeground(Theme.TEXT_PRIMARY); lblRounds.setBackground(bg);
        lblMeilleur.setFont(Theme.FONT_CARD); lblMeilleur.setForeground(Theme.ACCENT_GOLD); lblMeilleur.setBackground(bg);
        Button btnAccueil = new Button("RETOUR A L'ACCUEIL");
        btnAccueil.setFont(Theme.FONT_BTN); btnAccueil.setBackground(new Color(26,70,148)); btnAccueil.setForeground(Color.WHITE);
        btnAccueil.addActionListener(e -> { jeu.allerA("accueil"); });
        Panel boutons = new Panel(new FlowLayout(FlowLayout.CENTER)); boutons.setBackground(bg); boutons.add(btnAccueil);
        Label esp = new Label(""); esp.setBackground(bg);
        add(esp); add(lblResultat); add(lblRounds); add(lblMeilleur); add(new Label("")); add(boutons);
    }

    public void update(Observable o, Object arg) {
        if (m.getEtat() != Modele.Etat.FINI) return;
        card.show(parent, "fin");
        boolean gagne = m.getMessage().contains("VOUS GAGNEZ");
        if (gagne) {
            lblResultat.setText("VICTOIRE !"); lblResultat.setForeground(Theme.ACCENT_GREEN);
            if (!scoreEnregistre) { ScoreManager.sauvegarderSiMeilleur(m.getRoundNum()); scoreEnregistre=true; }
            Score best = ScoreManager.charger();
            if (best!=null && best.getNbRounds()==m.getRoundNum()) lblMeilleur.setText("NOUVEAU RECORD : "+best);
            else if (best!=null) lblMeilleur.setText("Meilleur score : "+best);
        } else {
            lblResultat.setText("DEFAITE..."); lblResultat.setForeground(Theme.ACCENT_RED);
            Score best = ScoreManager.charger();
            if (best!=null) lblMeilleur.setText("Meilleur score : "+best);
        }
        lblRounds.setText("Partie terminee en " + m.getRoundNum() + " rounds");
        repaint();
    }
}

    // affichage gagnant
