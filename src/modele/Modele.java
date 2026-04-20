package modele;

import java.util.Observable;

/**
 * Modele (logique du jeu) — pattern MVC.
 * Extends Observable : les vues s'abonnent et recoivent les mises a jour automatiquement.
 */
public class Modele extends Observable {

    public enum Etat { CHOISIR, LANCER, ACTION, FINI }

    private Etat    etat;
    private Joueur  j1 = new Joueur("Joueur");
    private Joueur  j2 = new Joueur("Ennemi");
    private boolean J1joue = true;
    private boolean koRemplace = false;
    private int     roundNum = 1;
    private String  message  = "Choisissez votre personnage actif";

    public Modele(java.util.List<Carte> deckJ1, java.util.List<Carte> deckJ2) {
        for (Carte c : deckJ1) j1.deck.ajouterCarte(c);
        for (Carte c : deckJ2) j2.deck.ajouterCarte(c);
        // Tirer un effet aleatoire entre 1 et 10 pour chaque joueur
        j1.effetEnMain = (int)(Math.random() * 10) + 1;
        j2.effetEnMain = (int)(Math.random() * 10) + 1;
        etat = Etat.CHOISIR;
    }

    // ── CHOISIR PERSONNAGE ────────────────────────────────────────────────────

    public void choisirPerso(int i) {
        if (etat != Etat.CHOISIR) return;
        Joueur joueur = J1joue ? j1 : j2;

        // Trouver la i-eme carte vivante
        Carte choix = null; int compte = 0;
        for (Carte c : joueur.deck.getCartes()) {
            if (c.getHp() > 0) { if (compte == i) choix = c; compte++; }
        }
        if (choix == null)
            for (Carte c : joueur.deck.getCartes())
                if (c.getHp() > 0) choix = c;
        if (choix == null) return;

        joueur.perso = choix;

        if (koRemplace) {
            koRemplace = false;
            etat = Etat.ACTION;
            updateView(joueur.nom + " de retour avec " + joueur.perso.getNom() + " !");
        } else if (J1joue) {
            J1joue = false;
            updateView("Vous avez choisi " + j1.perso.getNom() + " - L'adversaire choisit...");
        } else {
            etat = Etat.LANCER; J1joue = true;
            updateView("Pret ! Cliquez PIOCHER ENERGIES");
        }
    }

    // ── LANCER LES DES ───────────────────────────────────────────────────────

    public void lancer() {
        if (etat != Etat.LANCER) return;
        j1.des.relancer(); j2.des.relancer();
        j1.roundFini = false; j2.roundFini = false;
        etat = Etat.ACTION; J1joue = true;
        message = "ROUND " + roundNum + " - A vous !"; updateView();
    }

    // ── ATTAQUER ─────────────────────────────────────────────────────────────

    public void attaquer() {
        if (etat != Etat.ACTION) return;
        Joueur att = J1joue ? j1 : j2, def = J1joue ? j2 : j1;
        if (att.perso == null || def.perso == null) return;
        if (!att.consommerDes(1)) { updateView("Pas assez de des !"); return; }
        Son.playElement(att.perso.getElement());
        if (def.invincible) {
            def.invincible = false;
            message = def.perso.getNom() + " est invincible !"; switchTour(); return;
        }
        int d = calcDegats(att, def);
        def.perso.touche(d);
        message = att.perso.getNom() + " attaque pour " + d + " degats !";
        if (!verifierMort()) switchTour();
    }

    public void superAttaquer() {
        if (etat != Etat.ACTION) return;
        Joueur att = J1joue ? j1 : j2, def = J1joue ? j2 : j1;
        if (att.perso == null || def.perso == null) return;
        if (!att.consommerDes(2)) { updateView("Pas assez de des !"); return; }
        Son.playElement(att.perso.getElement());
        if (def.invincible) {
            def.invincible = false;
            message = def.perso.getNom() + " est invincible !"; switchTour(); return;
        }
        int d = calcDegats(att, def) * 2;
        def.perso.touche(d);
        message = att.perso.getNom() + " SUPER attaque pour " + d + " degats !";
        if (!verifierMort()) switchTour();
    }

    private int calcDegats(Joueur att, Joueur def) {
        int atk = att.perso.getAttaque() + att.rage;
        if (att.boost)    { atk *= 2;            att.boost    = false; }
        if (att.combo)    { atk = (int)(atk*1.5); att.combo   = false; }
        if (def.bouclier) { atk /= 2;             def.bouclier= false; }
        return Math.max(0, atk);
    }

    // ── SOIN ─────────────────────────────────────────────────────────────────

    /** Soin cible : clic sur une carte en main. Cout = 3 des. */
    public void healer(int i) {
        if (etat != Etat.ACTION) return;
        Joueur joueur = J1joue ? j1 : j2;
        if (joueur.perso == null) return;
        if (!joueur.consommerDes(3)) { updateView("Pas assez de des ! (3 energies)"); return; }
        int soin = Math.max(joueur.perso.getHeal(), 2); // minimum 2 HP
        Carte cible = joueur.deck.getCartes().get(i);
        cible.touche(-soin);
        message = cible.getNom() + " recupere " + soin + " HP"; switchTour();
    }

    /** Soin du personnage actif (bouton SOIN). Cout = 3 des. */
    public void healerActif() {
        if (etat != Etat.ACTION) return;
        Joueur joueur = J1joue ? j1 : j2;
        if (joueur.perso == null) return;
        if (!joueur.consommerDes(3)) { updateView("Pas assez de des ! (3 energies)"); return; }
        int soin = Math.max(joueur.perso.getHeal(), 2);
        joueur.perso.touche(-soin);
        message = joueur.perso.getNom() + " se soigne de " + soin + " HP"; switchTour();
    }

    // ── EFFETS SPECIAUX ───────────────────────────────────────────────────────

    public void utiliserEffet() {
        if (etat != Etat.ACTION) return;
        Joueur joueur = J1joue ? j1 : j2;
        if (joueur.effetEnMain == null)  { updateView("Pas de carte effet !"); return; }
        if (joueur.effetUtiliseCeTour)   { updateView("Effet deja utilise !"); return; }
        int id = joueur.effetEnMain;
        joueur.effetEnMain = null; joueur.effetUtiliseCeTour = true;
        appliquerEffet(id); updateView();
    }

    private void appliquerEffet(int id) {
        Joueur att = J1joue ? j1 : j2, def = J1joue ? j2 : j1;
        // Les effets 1-10 : chaque id correspond a un comportement distinct
        switch (id) {
            case 1:  att.boost = true; message = "BOOSTER ! Prochaine attaque x2"; break;
            case 2:  att.combo = true; message = "COMBO ! Prochaine attaque x1.5"; break;
            case 3:  if (att.perso!=null) att.perso.touche(-5);
                     message = "SOIN ! " + (att.perso!=null?att.perso.getNom():"") + " recupere 5 HP"; break;
            case 4:  if (att.perso!=null) att.perso.touche(-10);
                     message = "GRAND SOIN ! " + (att.perso!=null?att.perso.getNom():"") + " recupere 10 HP"; break;
            case 5:  att.invincible = true; message = "INVINCIBLE ! Immunite ce tour"; break;
            case 6:  for (Carte c : att.deck.getCartes()) c.touche(-2);
                     message = "PURIFICATION ! Toute l'equipe recupere 2 HP"; break;
            case 7:  att.bouclier = true; message = "BOUCLIER ! Prochain coup divise par 2"; break;
            case 8:  int ajout=0;
                     for (int i=0;i<8&&ajout<3;i++)
                         if (att.des.getElement(i)==null) { att.des.valeurs[i]="Omni"; ajout++; }
                     att.des.notifierObservateurs(); message = "DES BONUS ! +" + ajout + " Omni"; break;
            case 9:  if (def.perso!=null) def.perso.touche(4);
                     message = "ECLAIR ! 4 degats directs !"; verifierMort(); break;
            case 10: att.des.relancer(); att.des.notifierObservateurs();
                     message = "RELANCE ! Nouveaux des !"; break;
        }
    }

    // ── FIN DE TOUR ──────────────────────────────────────────────────────────

    public void tourTermine() {
        if (etat != Etat.ACTION) return;
        Joueur joueur = J1joue?j1:j2, autre = J1joue?j2:j1;
        if (joueur.roundFini) return;
        joueur.roundFini = true;
        if (autre.roundFini) nouveauRound();
        else { J1joue = !J1joue; updateView(joueur.nom + " a termine son tour"); }
    }

    private void switchTour() {
        if (etat != Etat.ACTION) return;
        Joueur autre = J1joue ? j2 : j1;
        if (!autre.roundFini) J1joue = !J1joue;
        updateView();
    }

    private void nouveauRound() {
        roundNum++;
        j1.resetRound(); j2.resetRound();
        if (j1.effetEnMain == null) j1.effetEnMain = (int)(Math.random()*10)+1;
        if (j2.effetEnMain == null) j2.effetEnMain = (int)(Math.random()*10)+1;
        etat = Etat.LANCER; J1joue = true;
        message = "ROUND " + roundNum + " - Cliquez PIOCHER ENERGIES"; updateView();
    }

    private boolean verifierMort() {
        if (j1.perso != null && j1.perso.getHp() <= 0) {
            boolean tousKO = true;
            for (Carte c : j1.deck.getCartes()) if (c.getHp()>0) tousKO=false;
            if (tousKO) { etat=Etat.FINI; message="L'ENNEMI GAGNE !"; updateView(); return true; }
            j1.perso.setEnsanglantee(true); j1.perso=null;
            etat=Etat.CHOISIR; J1joue=true; koRemplace=true;
            j1.roundFini=false; j2.roundFini=false;
            updateView("Votre perso est KO ! Choisissez-en un autre"); return true;
        }
        if (j2.perso != null && j2.perso.getHp() <= 0) {
            boolean tousKO = true;
            for (Carte c : j2.deck.getCartes()) if (c.getHp()>0) tousKO=false;
            if (tousKO) { etat=Etat.FINI; message="VOUS GAGNEZ !"; updateView(); return true; }
            j2.perso.setEnsanglantee(true); j2.perso=null;
            etat=Etat.CHOISIR; J1joue=false; koRemplace=true;
            j1.roundFini=false; j2.roundFini=false;
            updateView("Ennemi KO ! Il choisit un nouveau personnage..."); return true;
        }
        return false;
    }

    private void updateView()           { setChanged(); notifyObservers(); }
    private void updateView(String msg) { message=msg; updateView(); }

    // ── GETTERS ──────────────────────────────────────────────────────────────
    public Etat    getEtat()          { return etat; }
    public Deck    getMainJ1()        { return j1.deck; }
    public Deck    getMainJ2()        { return j2.deck; }
    public Des     getDesJ1()         { return j1.des; }
    public Des     getDesJ2()         { return j2.des; }
    public Carte   getPersoJ1()       { return j1.perso; }
    public Carte   getPersoJ2()       { return j2.perso; }
    public int     getRoundNum()      { return roundNum; }
    public String  getMessage()       { return message; }
    public boolean isJ1TourFini()     { return j1.roundFini; }
    public boolean isJ2TourFini()     { return j2.roundFini; }
    public boolean isJ1joue()         { return J1joue; }
    public Integer getEffetJ1()       { return j1.effetEnMain; }
    public Integer getEffetJ2()       { return j2.effetEnMain; }
    public boolean isEffetUtiliseJ1() { return j1.effetUtiliseCeTour; }
    public boolean isEffetUtiliseJ2() { return j2.effetUtiliseCeTour; }
}
