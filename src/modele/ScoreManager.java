package modele;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Sauvegarde et charge le classement des meilleurs scores.
 *
 * Structure : TreeSet<Score> trie automatiquement par nombre de rounds (ordre naturel).
 * Persistance : serialisation binaire dans res/scores.dat.
 */
public class ScoreManager {
    private static final String FICHIER    = "res/scores.dat";
    private static final int    MAX_SCORES = 5;
    @SuppressWarnings("unchecked")
    public static TreeSet<Score> chargerClassement() {
        File f = new File(FICHIER);
        if (!f.exists()) return new TreeSet<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof TreeSet)  return (TreeSet<Score>) obj;
            if (obj instanceof Score) {
                TreeSet<Score> ts = new TreeSet<>(); ts.add((Score) obj); return ts;
            }
        } catch (Exception e) { System.out.println("Erreur scores : " + e.getMessage()); }
        return new TreeSet<>();
    }
    public static Score charger() {
        TreeSet<Score> ts = chargerClassement();
        return ts.isEmpty() ? null : ts.first();
    }
    public static void sauvegarderSiMeilleur(int nbRounds) {
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        TreeSet<Score> classement = chargerClassement();
        classement.add(new Score(nbRounds, date));
        while (classement.size() > MAX_SCORES) classement.pollLast();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER))) {
            oos.writeObject(classement);
        } catch (Exception e) { System.out.println("Erreur sauvegarde : " + e.getMessage()); }
    }
}
    // serialisation

    // treeset scores
