
import java.util.Observable;

public class Carte extends Observable {
    private String nom;
    private int pv;
    private int attaque;
    private String type;

    public Carte(String nom, int pv, int attaque, String type) {
        this.nom = nom;
        this.pv = pv;
        this.attaque = attaque;
        this.type = type;
    }
    
    
    
    
    //Carte à reçu des dégats:
    public void touché(int degats) { //Previent la Vue quand elle change
        this.pv -= degats;
        setChanged(); // Marque le changement
        notifyObservers(); // Prévient
    }

    
    
    
    public String getNom() {return nom;}
    public int getPv() {return pv;}
    public int getAttaque() {return attaque;}
}