package modele;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
/**
 * Base de données des cartes personnages, chargée depuis res/cartes.csv.
 *
 * Pourquoi ces structures ?
 *   ArrayList    → stocker toutes les cartes dans l'ordre
 *   HashMap      → trouver une carte par ID en O(1) (comme un dictionnaire)
 *   TreeMap      → grouper les cartes par élément, trié A-Z automatiquement
 */
public class CarteDB {
    private static List<Carte>                  toutes  = new ArrayList<>();
    private static HashMap<Integer, Carte>      parId   = new HashMap<>();
    private static TreeMap<String, List<Carte>> parElem = new TreeMap<>();
    // Comparateurs réutilisables dans VueIndex
    public static final Comparator<Carte> PAR_HP_DESC  = (a, b) -> b.getHpMax()   - a.getHpMax();
    public static final Comparator<Carte> PAR_ATK_DESC = (a, b) -> b.getAttaque() - a.getAttaque();
    public static final Comparator<Carte> PAR_NOM_ASC  = Comparator.comparing(Carte::getNom);
    // Chargement au démarrage du programme (bloc static = exécuté une seule fois)
    static {
        try {
            BufferedReader br = new BufferedReader(new FileReader("res/cartes.csv"));
            br.readLine(); // sauter la ligne d'en-tête

            String ligne = br.readLine();
            while (ligne != null) {
                String[] col = ligne.split(";");
                if (col.length >= 6) {
                    int    id      = Integer.parseInt(col[0].trim());
                    String nom     = col[1].trim();
                    int    hp      = Integer.parseInt(col[2].trim());
                    int    attaque = Integer.parseInt(col[3].trim());
                    int    heal    = Integer.parseInt(col[4].trim());
                    String element = col[5].trim();

                    Carte c = new Carte(nom, hp, attaque, heal, element, id);

                    toutes.add(c);
                    parId.put(id, c); // HashMap : clé = id, valeur = carte

                    // Ajouter à la liste de son élément dans le TreeMap
                    if (!parElem.containsKey(element)) {
                        parElem.put(element, new ArrayList<>());
                    }
                    parElem.get(element).add(c);
                }
                ligne = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            System.out.println("Erreur lecture cartes.csv : " + e.getMessage());
        }
    }

    /**
     * Retourne une copie fraîche d'une carte par son ID.
     * HashMap.get(id) = accès direct en O(1), sans parcourir la liste.
     */
    public static Carte getCarte(int id) {
        Carte c = parId.get(id);
        if (c == null) return null;
        return new Carte(c.getNom(), c.getHpMax(), c.getAttaque(),
                         c.getHeal(), c.getElement(), c.getId());
    }

    /** Copie de toutes les cartes (nouvelles instances pour éviter les modifications). */
    public static List<Carte> toutes() {
        List<Carte> copie = new ArrayList<>();
        for (Carte c : toutes) {
            copie.add(new Carte(c.getNom(), c.getHpMax(), c.getAttaque(),
                                c.getHeal(), c.getElement(), c.getId()));
        }
        return copie;
    }

    /**
     * Filtrage multi-critères avec tri optionnel (Collections.sort + Comparator).
     * Si un élément est spécifié, on cherche dans le TreeMap (plus rapide).
     */
    public static List<Carte> filtrer(String nom, String element,
                                       int minHp, int minAtk, Comparator<Carte> tri) {
        // Choisir la source : sous-liste de l'élément ou toutes les cartes
        List<Carte> source;
        if (!element.isEmpty() && parElem.containsKey(element)) {
            source = parElem.get(element); // TreeMap : accès direct O(log n)
        } else {
            source = toutes;
        }

        // Appliquer les filtres
        List<Carte> resultat = new ArrayList<>();
        for (Carte c : source) {
            boolean nomOk = nom.isEmpty()
                            || c.getNom().toLowerCase().contains(nom.toLowerCase());
            boolean hpOk  = c.getHpMax()   >= minHp;
            boolean atkOk = c.getAttaque() >= minAtk;
            if (nomOk && hpOk && atkOk) {
                resultat.add(new Carte(c.getNom(), c.getHpMax(), c.getAttaque(),
                                       c.getHeal(), c.getElement(), c.getId()));
            }
        }

        // Trier si demandé
        if (tri != null) {
            Collections.sort(resultat, tri); // O(n log n) — plus rapide que tri à bulles
        }
        return resultat;
    }

    public static int taille() { return toutes.size(); }
}

    // parsing csv virgules
