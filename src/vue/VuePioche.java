package vue;

import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import modele.Modele;
import modele.CarteEffetDB;
import modele.Son;

/** Zone d'effet special d'un joueur avec bouton UTILISER. */
public class VuePioche extends Panel implements Observer {

    private final Modele modele; private final boolean estJ1;
    private Button btnUtiliser;
    private Label  lblNom;
    private TextArea aireDesc;
    private Panel  zoneImg;

    public VuePioche(Modele m, boolean j1) {
        modele=m; estJ1=j1; m.addObserver(this);
        setBackground(Theme.BG_DARK); setLayout(new BorderLayout(3,3));

        Color c=j1?Theme.ACCENT_BLUE:new Color(180,50,30);
        Label titre=new Label(j1?"MON EFFET":"EFFET ENNEMI",Label.CENTER);
        titre.setFont(Theme.FONT_CARD); titre.setForeground(c); titre.setBackground(Theme.BG_DARK);
        add(titre, BorderLayout.NORTH);

        zoneImg = new Panel() {
            public void paint(Graphics g) { dessinerEffet(g); }
            public void update(Graphics g) { paint(g); }
        };
        zoneImg.setPreferredSize(new Dimension(0, 115));

        lblNom = new Label("", Label.CENTER);
        lblNom.setFont(Theme.FONT_CARD); lblNom.setForeground(new Color(130,145,210)); lblNom.setBackground(new Color(20,18,38));
        aireDesc = new TextArea("", 3, 15, TextArea.SCROLLBARS_NONE);
        aireDesc.setEditable(false); aireDesc.setBackground(new Color(28,28,48));
        aireDesc.setForeground(new Color(210,210,230)); aireDesc.setFont(Theme.FONT_ITALIC);

        Panel textes = new Panel(new BorderLayout(0,2)); textes.setBackground(new Color(22,20,38));
        textes.add(lblNom, BorderLayout.NORTH); textes.add(aireDesc, BorderLayout.CENTER);
        Panel centre = new Panel(new BorderLayout(0,3)); centre.setBackground(new Color(22,20,38));
        centre.add(zoneImg, BorderLayout.NORTH); centre.add(textes, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        if (j1) {
            btnUtiliser = new Button("UTILISER L'EFFET");
            btnUtiliser.setBackground(new Color(70,80,150)); btnUtiliser.setForeground(Color.WHITE);
            btnUtiliser.setFont(Theme.FONT_BTN);
            btnUtiliser.addActionListener(e -> { modele.utiliserEffet(); });
            add(btnUtiliser, BorderLayout.SOUTH);
        }
    }

    private void dessinerEffet(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setColor(new Color(18,16,32)); g.fillRect(0, 0, zoneImg.getWidth(), zoneImg.getHeight());
        Integer id = estJ1?modele.getEffetJ1():modele.getEffetJ2();
        if (id == null) {
            g.setColor(new Color(55,55,80)); g.drawRect(3, 3, zoneImg.getWidth()-6, zoneImg.getHeight()-6);
            g.setColor(Theme.TEXT_DIM); g.setFont(Theme.FONT_ITALIC); g.drawString("Aucun effet", 12, zoneImg.getHeight()/2);
        } else {
            java.awt.image.BufferedImage img = Images.effet(id);
            if (img != null) g.drawImage(img, 3, 3, zoneImg.getWidth()-6, zoneImg.getHeight()-6, zoneImg);
            else { g.setColor(new Color(40,70,25)); g.fillRect(3, 3, zoneImg.getWidth()-6, zoneImg.getHeight()-6);
                   g.setColor(Color.WHITE); g.setFont(Theme.FONT_ROUND); g.drawString("#"+id, zoneImg.getWidth()/2-14, zoneImg.getHeight()/2+8); }
        }
    }

    public void update(Observable o, Object arg) {
        Integer id = estJ1?modele.getEffetJ1():modele.getEffetJ2();
        if (id != null) { lblNom.setText(CarteEffetDB.getNom(id)); aireDesc.setText(CarteEffetDB.getDesc(id)); }
        else            { lblNom.setText(""); aireDesc.setText("Nouvelle carte en debut de round"); }
        if (estJ1) {
            boolean monTour = modele.isJ1joue() && modele.getEtat()==Modele.Etat.ACTION;
            btnUtiliser.setEnabled(monTour && id!=null && !modele.isEffetUtiliseJ1());
        }
        zoneImg.repaint();
    }
}
