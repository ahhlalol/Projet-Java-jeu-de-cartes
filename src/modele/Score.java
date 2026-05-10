package modele;
import java.io.Serializable;
/**
 * Un score : nombre de rounds pour gagner + date.
 * Implements Comparable pour etre classe dans un TreeSet (moins de rounds = meilleur).
 * serialVersionUID = 1L pour compatibilite avec les anciens fichiers scores.dat.
 */
public class Score implements Serializable, Comparable<Score> {

    private static final long serialVersionUID = 1L;
    private final int    nbRounds;
    private final String date;

    public Score(int nbRounds, String date) {
        this.nbRounds = nbRounds; this.date = date;
    }

    @Override
    public int compareTo(Score other) {
        int cmp = Integer.compare(this.nbRounds, other.nbRounds);
        return cmp != 0 ? cmp : this.date.compareTo(other.date);
    }

    public int    getNbRounds() { return nbRounds; }
    public String getDate()     { return date; }

    @Override public String toString() { return nbRounds + " rounds  (" + date + ")"; }
}

    // compareTo
