package vue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * CACHE D'IMAGES — LinkedHashMap LRU (60 entrées max)
 *
 * LRU (Least Recently Used) :
 *   - accessOrder=true : chaque accès déplace l'entrée en fin de liste
 *   - removeEldestEntry() supprime la plus ancienne quand on dépasse MAX
 *   - Images fréquentes restent en cache, les rares sont évincées
 *
 * Préchargement :
 *   prechargerCartes(ids) charge plusieurs images d'un coup AVANT le premier
 *   paint() — les accès pendant le rendu sont tous O(1) depuis la RAM,
 *   sans aucun accès disque (qui causerait un hitch visible).
 */
public class Images {

    private static final int MAX = 60;

    private static final Map<String, BufferedImage> cache =
        new LinkedHashMap<String, BufferedImage>(MAX, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) {
                return size() > MAX;
            }
        };

    /** Retourne l'image (depuis le cache ou le disque). null si fichier absent. */
    public static BufferedImage get(String chemin) {
        if (!cache.containsKey(chemin)) {
            try   { cache.put(chemin, ImageIO.read(new File(chemin))); }
            catch (Exception e) { cache.put(chemin, null); }
        }
        return cache.get(chemin);
    }

    public static BufferedImage carte(int id) { return get("res/cartes/" + id + ".png"); }
    public static BufferedImage effet(int id) { return get("res/effets/" + id + ".png"); }

    /**
     * Précharge les images d'une liste de cartes.
     * À appeler AVANT de démarrer une partie ou d'afficher une grille,
     * pour éviter les accès disque pendant paint().
     *
     * Exemple : Images.prechargerCartes(Arrays.asList(1, 5, 12, 34));
     */
    public static void prechargerCartes(Collection<Integer> ids) {
        for (int id : ids) carte(id);
    }

    /** Précharge toutes les images de cartes (utile pour l'Index). */
    public static void prechargerToutesCartes(int nbCartes) {
        for (int i = 0; i <= nbCartes; i++) carte(i); // 0 = dos de carte
    }
}
