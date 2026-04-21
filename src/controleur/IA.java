package controleur;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;
import modele.Carte;
import modele.Deck;
import modele.Des;
import modele.Modele;

public class IA implements Observer {

    Modele  modele;
    boolean enTrainDeJouer = false;

    public IA(Modele m) {
        modele = m;
        m.addObserver(this);
    }

    public void update(Observable o, Object arg) {
        if (modele.isJ1joue()) return;
        if (enTrainDeJouer)    return;

        if (modele.getEtat() == Modele.Etat.CHOISIR) {
            enTrainDeJouer = true;
            new Timer(600, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((Timer) e.getSource()).stop();
                    // On libere le verrou AVANT d'appeler choisirPerso
                    // parce que choisirPerso() appelle updateView() qui rappelle update()
                    // et si enTrainDeJouer est encore true, l'IA ne joue pas apres
                    enTrainDeJouer = false;
                    choisirPerso();
                    // Si apres avoir choisi c'est toujours le tour de l'IA (cas KO),
                    // on relance manuellement
                    if (!modele.isJ1joue() && modele.getEtat() == Modele.Etat.ACTION) {
                        jouerApresDelai();
                    }
                }
            }).start();
        }

        if (modele.getEtat() == Modele.Etat.ACTION) {
            jouerApresDelai();
        }
    }

    void jouerApresDelai() {
        if (enTrainDeJouer) return;
        enTrainDeJouer = true;
        new Timer(900, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                enTrainDeJouer = false;
                jouer();
                // Si c'est encore le tour de l'IA apres cette action, on continue
                if (!modele.isJ1joue() && modele.getEtat() == Modele.Etat.ACTION) {
                    jouerApresDelai();
                }
            }
        }).start();
    }

    void choisirPerso() {
        Deck deck        = modele.getMainJ2();
        int  meilleurIdx = 0;
        int  meilleurHP  = -1;
        for (int i = 0; i < deck.getCartes().size(); i++) {
            Carte c = deck.getCartes().get(i);
            if (c.getHp() > meilleurHP) {
                meilleurHP  = c.getHp();
                meilleurIdx = i;
            }
        }
        modele.choisirPerso(meilleurIdx);
    }

    void jouer() {
        Carte perso = modele.getPersoJ2();
        Des   des   = modele.getDesJ2();
        if (perso == null) return;

        String elem      = perso.getElement();
        int    desDispos = des.compterElement(elem) + des.compterElement("Omni");

        if (modele.getEffetJ2() != null && !modele.isEffetUtiliseJ2()) {
            modele.utiliserEffet();
            return;
        }

        if (desDispos >= 2) { modele.superAttaquer(); return; }
        if (desDispos >= 1) { modele.attaquer();      return; }

        modele.tourTermine();
    }
}
