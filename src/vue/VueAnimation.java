package vue;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;
import modele.Modele;
/** GIF anime central (2s) selon le type d'action en cours. */
public class VueAnimation extends Panel implements Observer {
    private final Image gifAttaque     = Toolkit.getDefaultToolkit().getImage("res/gifs/attaque.gif");
    private final Image gifHealed      = Toolkit.getDefaultToolkit().getImage("res/gifs/healed.gif");
    private final Image gifFrustration = Toolkit.getDefaultToolkit().getImage("res/gifs/frustration.gif");
    private final Image gifRage        = Toolkit.getDefaultToolkit().getImage("res/gifs/rage.gif");
    private Image gifActuel = null;
    private boolean visible = false;
    private Timer timerCache;

    public VueAnimation(Modele m) {
        m.addObserver(this);
        setBackground(new Color(14, 12, 26));
        setPreferredSize(new Dimension(0, 220));
    }

    public void update(Graphics g) { paint(g); }

    public void update(Observable o, Object arg) {
        String msg = ((Modele)o).getMessage();
        if      (msg.contains("SUPER attaque"))                        montrer(gifRage);
        else if (msg.contains("attaque"))                              montrer(gifAttaque);
        else if (msg.contains("recupere")||msg.contains("SOIN")
              || msg.contains("PURIFICATION")||msg.contains("soigne")) montrer(gifHealed);
        else if (msg.contains("BOOSTER")||msg.contains("COMBO")
              || msg.contains("ECLAIR"))                               montrer(gifRage);
        else if (msg.contains("BOUCLIER")||msg.contains("INVINCIBLE")
              || msg.contains("RELANCE"))                              montrer(gifAttaque);
        else if (msg.contains("Pas assez")||msg.contains("bloquee"))  montrer(gifFrustration);
        else { visible=false; repaint(); }
    }

    private void montrer(Image gif) {
        gifActuel=gif; visible=true;
        if (timerCache!=null) timerCache.stop();
        timerCache = new Timer(2000, e -> { visible=false; repaint(); ((Timer)e.getSource()).stop(); });
        timerCache.start(); repaint();
    }

    public void paint(Graphics g) {
        g.setColor(new Color(14, 12, 26));
        g.fillRect(0, 0, getWidth(), getHeight());
        if (visible && gifActuel != null) {
            int t=198, x=(getWidth()-t)/2, y=(getHeight()-t)/2;
            g.drawImage(gifActuel, x, y, t, t, this);
        }
    }
}

    // 4 gifs contextuels
