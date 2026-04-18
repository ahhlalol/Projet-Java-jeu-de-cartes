package modele;
import java.util.Observable;
import java.util.Random;
/** 8 des d'energie. Extends Observable pour notifier VueDes a chaque relance. */
public class Des extends Observable {
    public static final String[] ELEMENTS = {"Vent","Pierre","Foudre","Eau","Feu","Glace","Nature","Omni"};
    public String[] valeurs = new String[8];
    private Random r = new Random();
    public Des() { for (int i=0;i<8;i++) valeurs[i]=null; }
    public void relancer() {
        for (int i=0;i<8;i++) valeurs[i]=ELEMENTS[r.nextInt(8)];
        setChanged(); notifyObservers();
    }
    public void notifierObservateurs() { setChanged(); notifyObservers(); }
    public String getElement(int i) { return valeurs[i]; }
    public int compterElement(String elem) {
        int n=0; for (String v:valeurs) if (elem.equals(v)) n++; return n;
    }
    public void consommerElement(String elem, int n) {
        int r2=n;
        for (int i=0;i<8&&r2>0;i++) if (elem.equals(valeurs[i])) { valeurs[i]=null; r2--; }
        for (int i=0;i<8&&r2>0;i++) if ("Omni".equals(valeurs[i])) { valeurs[i]=null; r2--; }
        setChanged(); notifyObservers();
    }
}
