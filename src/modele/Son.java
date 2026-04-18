package modele;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;

/**
 * GESTION DES SONS — cache en RAM pour une lecture instantanée
 *
 * Les fichiers .wav sont dans res/Sons/ (ex: res/Sons/Sound_Fire.wav).
 * prechargerTout() les lit une seule fois au démarrage et stocke les
 * octets en mémoire (HashMap<String, byte[]>).
 * play() relit depuis la RAM → O(1), zéro accès disque.
 * Chaque son est joué dans un Thread séparé pour ne pas bloquer l'UI.
 */
public class Son {

    private static final String DOSSIER = "res/Sons/";

    // Cache : nom du fichier → contenu binaire en mémoire
    private static final HashMap<String, byte[]> cache = new HashMap<>();

    /** Précharge tous les sons au démarrage (appelé une seule fois dans Jeu.java). */
    public static void prechargerTout() {
        precharger("Sound_Fire.wav");
        precharger("Sound_Ice.wav");
        precharger("Sound_Foudre.wav");
        precharger("Sound_Vent.wav");
        precharger("Sound-Eau.wav");
        precharger("sound_Pierre.wav");
        precharger("sound_Nature.wav");
        precharger("clic.wav");
    }

    /**
     * Lit un fichier son depuis res/Sons/ et le stocke en RAM.
     * Si le fichier est absent, stocke null → silence sans crash.
     */
    public static void precharger(String nom) {
        if (cache.containsKey(nom)) return;

        File f = new File(DOSSIER + nom);
        if (!f.exists()) {
            System.out.println("Son non trouve : " + DOSSIER + nom);
            cache.put(nom, null);
            return;
        }

        try (InputStream src = new FileInputStream(f)) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int n;
            while ((n = src.read(tmp)) != -1) buf.write(tmp, 0, n);
            cache.put(nom, buf.toByteArray());
        } catch (Exception e) {
            cache.put(nom, null);
        }
    }

    /**
     * Joue un son depuis le cache (RAM → Clip audio).
     * Thread séparé pour ne pas bloquer l'interface.
     */
    public static void play(String nom) {
        byte[] data = cache.get(nom);
        if (data == null) return;

        new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new ByteArrayInputStream(data));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception ignored) {}
        }).start();
    }

    /**
     * Joue le son de l'élément du personnage qui attaque.
     * Appelé dans Modele.attaquer() et Modele.superAttaquer().
     */
    public static void playElement(String elem) {
        switch (elem) {
            case "Feu":    play("Sound_Fire.wav");   break;
            case "Glace":  play("Sound_Ice.wav");    break;
            case "Foudre": play("Sound_Foudre.wav"); break;
            case "Vent":   play("Sound_Vent.wav");   break;
            case "Eau":    play("Sound-Eau.wav");    break;
            case "Pierre": play("sound_Pierre.wav"); break;
            case "Nature": play("sound_Nature.wav"); break;
        }
    }

    public static void playClic() { play("clic.wav"); }
}
