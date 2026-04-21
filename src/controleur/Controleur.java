package controleur;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import modele.Modele;


/**
 * Controleur MVC : recoit les clics des boutons et appelle le Modele.
 * Joue un son de clic avant chaque action pour un retour immediat.
 */
public class Controleur implements ActionListener {

    private final Modele mdl;

    public Controleur(Modele m) { mdl = m; }

    @Override
    public void actionPerformed(ActionEvent e) {
        String nom = ((Button) e.getSource()).getName();
        
        switch (nom) {
            case "LANCER": mdl.lancer();        break;
            case "ATK":    mdl.attaquer();       break;
            case "SUPER":  mdl.superAttaquer();  break;
            case "ROUND":  mdl.tourTermine();    break;
            case "SOIN":   mdl.healerActif();    break;
        }
    }
}
