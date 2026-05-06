package modele;
/** Etat d'un joueur : deck, des, perso actif, drapeaux d'etat. */
public class Joueur {
    String  nom;
    Deck    deck  = new Deck();
    Des     des   = new Des();
    Carte   perso = null;
    boolean roundFini=false, effetUtiliseCeTour=false;
    boolean boost=false, combo=false, bouclier=false, invincible=false;
    int     rage=0;
    Integer effetEnMain = null;
    public Joueur(String nom) { this.nom=nom; }
    /** Consomme n des de l'element du perso actif (+ Omni si besoin). */
    public boolean consommerDes(int n) {
        String elem = perso.getElement();
        if (des.compterElement(elem)+des.compterElement("Omni")<n) return false;
        des.consommerElement(elem, n);
        return true;
    }
    public void resetRound() { roundFini=false; rage=0; invincible=false; effetUtiliseCeTour=false; }
}

    // flags
