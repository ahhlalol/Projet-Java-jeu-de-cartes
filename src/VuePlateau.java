
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class VuePlateau extends Frame implements WindowListener{
    private Panel mainJoueur;

    public VuePlateau() {
        this.setTitle("Jeu de FOU");
        this.setSize(800, 600);
        this.setLayout(new BorderLayout());
        this.addWindowListener(this);

        mainJoueur = new Panel();
        mainJoueur.setBackground(Color.LIGHT_GRAY);
        this.add(mainJoueur, BorderLayout.SOUTH);

    }

    public void ajouterCarte(Carte c) {
    	mainJoueur.add(new VueCarte(c));
        this.validate(); // Rafraîchit l'affichage
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //Juste pouvoir fermer la fenetre
    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}
	@Override public void windowClosing(WindowEvent e) {System.exit(0);}
}