package modele;
import java.util.Observable;
/** Carte personnage. Extends Observable : notifie les vues quand les HP changent. */
public class Carte extends Observable {
    private int id, hp, hpMax, attaque, heal;
    private String nom, element;
    private boolean ensanglantee = false;
    public Carte(String nom, int hp, int attaque, int heal, String element, int id) {
        this.id=id; this.nom=nom; this.hp=hp; this.hpMax=hp;
        this.attaque=attaque; this.heal=heal; this.element=element;
    }
    /** Degats (positif) ou soin (negatif). Si soin ramene HP>0 : resurrection. */
    public void touche(int degats) {
        hp -= degats;
        if (hp > hpMax) hp = hpMax;
        if (hp < 0)     hp = 0;
        if (hp > 0 && ensanglantee) ensanglantee = false;
        setChanged(); notifyObservers();
    }
    public int    getId()      { return id; }
    public String getNom()     { return nom; }
    public int    getHp()      { return hp; }
    public int    getHpMax()   { return hpMax; }
    public int    getAttaque() { return attaque; }
    public int    getHeal()    { return heal; }
    public String getElement() { return element; }
    public boolean isEnsanglantee()           { return ensanglantee; }
    public void    setEnsanglantee(boolean b) { ensanglantee = b; }
}
