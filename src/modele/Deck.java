package modele;
import java.util.ArrayList;
import java.util.List;
/** Contient les 3 cartes d'un joueur. */
public class Deck {
    private List<Carte> cartes = new ArrayList<>();
    public void ajouterCarte(Carte c) { cartes.add(c); }
    public List<Carte> getCartes()    { return cartes; }
}
