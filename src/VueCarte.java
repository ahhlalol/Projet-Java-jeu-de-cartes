import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class VueCarte extends Canvas implements Observer {
    private Carte modele;

    public VueCarte(Carte modele) {
        this.modele = modele;
        this.modele.addObserver(this);
        this.setPreferredSize(new Dimension(100, 150));
    }

    @Override
    public void paint(Graphics g) {
        //Forme de la carte
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, 99, 149, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(0, 0, 99, 149, 15, 15);
        
        g.drawString(modele.getNom(), 10, 20);
        g.setColor(Color.GREEN);
        g.drawString("PV: " + modele.getPv(), 10, 40);
        g.setColor(Color.RED);
        g.drawString("ATK: " + modele.getAttaque(), 10, 130);
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint(); // Redessine quand le modèle change
    }
}