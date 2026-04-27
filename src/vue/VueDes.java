package vue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import modele.*;

/** Affiche les 8 des d'energie d'un joueur. */
public class VueDes extends Panel implements Observer {

    private final Modele modele; private final boolean estJ1;
    private final Map<String,BufferedImage> imgElem = new HashMap<>();

    public VueDes(Modele m, boolean j1) {
        modele=m; estJ1=j1; m.addObserver(this);
        setBackground(Theme.BG_PANEL); setPreferredSize(new Dimension(220, 300));
        String[][] pairs = {{"Feu","fire.jpg"},{"Eau","water.jpg"},{"Vent","wind.jpg"},
            {"Foudre","foudre.jpg"},{"Pierre","rock.png"},{"Glace","ice.jpg"},
            {"Nature","nature.jpg"},{"Omni","4elements.png"}};
        for (String[] p:pairs) {
            try { imgElem.put(p[0], ImageIO.read(new File("res/images/"+p[1]))); }
            catch (IOException ignored) {}
        }
    }

    public void update(Graphics g) { paint(g); }

    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Theme.BG_PANEL); g.fillRect(0, 0, getWidth(), getHeight());

        Des   des  = estJ1 ? modele.getDesJ1() : modele.getDesJ2();
        Carte perso= estJ1 ? modele.getPersoJ1() : modele.getPersoJ2();
        Color titreColor = estJ1 ? Theme.ACCENT_BLUE : new Color(200,55,40);

        g.setColor(titreColor); g.fillRect(0, 0, 4, getHeight());
        g.setFont(Theme.FONT_CARD);
        g.drawString(estJ1?"ENERGIES JOUEUR":"ENERGIES ADVERSAIRE", 10, 17);

        String elemReq = perso != null ? perso.getElement() : "";
        for (int i=0; i<8; i++) {
            int x=10+(i%2)*103, y=26+(i/2)*64;
            String elem = des.getElement(i);
            if (elem==null) {
                g.setColor(new Color(32,30,52)); g.fillRoundRect(x, y, 92, 52, 8, 8);
            } else {
                BufferedImage img = imgElem.get(elem);
                if (img != null) {
                    Shape old = g.getClip();
                    g.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, 92, 52, 8, 8));
                    g.drawImage(img, x, y, 92, 52, this); g.setClip(old);
                } else { g.setColor(Theme.couleurElement(elem)); g.fillRoundRect(x, y, 92, 52, 8, 8); }
                boolean utile = elem.equals(elemReq)||elem.equals("Omni");
                if (utile) {
                    g.setColor(new Color(255,255,255,50)); g.fillRoundRect(x, y, 92, 52, 8, 8);
                    g.setColor(Theme.BORDER_ACTIVE); g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(x, y, 92, 52, 8, 8); g.setStroke(new BasicStroke(1));
                }
                g.setColor(Color.WHITE); g.setFont(Theme.FONT_CARD); g.drawString(elem, x+5, y+32);
            }
        }
    }

    public void update(Observable o, Object arg) { repaint(); }
}
