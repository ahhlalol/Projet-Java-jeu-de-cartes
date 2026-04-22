package vue;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;


/**
 * Bouton cliquable avec une image PNG a la place du texte.
 * Joue un son de clic automatiquement quand il est actif.
 */
public class BoutonImage extends Canvas {

    private final BufferedImage img;
    private ActionListener listener;
    private boolean actif = true;

    public BoutonImage(String chemin) {
        img = Images.get(chemin);
        if (img != null) {
            double s = 0.15;
            setPreferredSize(new Dimension((int)(img.getWidth()*s), (int)(img.getHeight()*s)));
        } else {
            setPreferredSize(new Dimension(140, 55));
        }
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (actif && listener != null) {
                    
                    listener.actionPerformed(new ActionEvent(BoutonImage.this, 0, ""));
                }
            }
        });
    }

    public void setActif(boolean b) { actif = b; repaint(); }
    public void addActionListener(ActionListener l) { listener = l; }

    public void paint(Graphics g) {
        if (img == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (!actif) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
    public void update(Graphics g) { paint(g); }
}
