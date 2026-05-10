package modele;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Base de donnees des effets speciaux (res/effets.csv).
 * HashMap<Integer,String> : acces au nom et a la description en O(1) par id.
 */
public class CarteEffetDB {

    private static final HashMap<Integer,String> noms  = new HashMap<>();
    private static final HashMap<Integer,String> descs = new HashMap<>();

    static {
        try (BufferedReader br = new BufferedReader(new FileReader("res/effets.csv"))) {
            br.readLine(); // en-tete
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] col = ligne.split(";", 3);
                if (col.length < 3) continue;
                int    id   = Integer.parseInt(col[0].trim());
                String nom  = col[1].trim();
                String desc = col[2].trim();
                noms.put(id, nom);
                descs.put(id, desc);
            }
        } catch (Exception e) {
            System.out.println("Erreur lecture effets.csv : " + e.getMessage());
        }
    }

    public static String getNom(int id)  { return noms.getOrDefault(id,  "Effet " + id); }
    public static String getDesc(int id) { return descs.getOrDefault(id, ""); }
    public static int    nbEffets()      { return noms.size(); }
}
