package vue;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import modele.Modele;

/**
 * Bandeau numéro de round — affiché en haut du panneau gauche.
 *
 * Affiche seulement "ROUND N" en grande police dorée.
 * Le message d'événement (attaque, soin…) est affiché en bas de l'écran
 * dans le bandeau SOUTH de VuePlateau, pour ne pas encombrer ici.
 */
public class VueMessage extends Panel implements Observer {

    private final Modele modele;

    public VueMessage(Modele m) {
        modele = m;
        m.addObserver(this);
        setBackground(Theme.BG_DARK);
        setPreferredSize(new Dimension(222, 55)); // moins haut : on n'affiche plus le msg
    }

    public void update(Graphics g) { paint(g); }

    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Theme.BG_DARK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // "ROUND N" en grande police Cinzel dorée
        g.setColor(Theme.ACCENT_GOLD);
        g.setFont(Theme.FONT_ROUND);
        g.drawString("ROUND  " + modele.getRoundNum(), 12, 38);

        // Séparateur doré en bas du bandeau
        g.setColor(new Color(210, 168, 45, 90));
        g.fillRect(8, getHeight() - 3, getWidth() - 16, 2);
    }

    public void update(Observable o, Object arg) { repaint(); }
}

    // message ok
