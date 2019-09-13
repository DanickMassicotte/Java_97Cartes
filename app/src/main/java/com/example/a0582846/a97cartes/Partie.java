package com.example.a0582846.a97cartes;

import java.util.Collections;
import java.util.Vector;

/**
 * Classe représentant une partie du jeu 97Cartes. Contient les cartes du paquet de cartes,
 * les cartes présentes dans la main de l'utilisateur ainsi que les cartes présentement sur
 * les piles augmentantes et descendantes.
 *
 * @author Danick Massicotte
 * @version 1
 */
public class Partie {

    // Variables d'instances
    private Vector<Carte> mCartes;
    private Vector<String> mMain;
    private Vector<String> mPile_98;
    private Vector<String> mPile_1;

    // Méthode constructeur
    public Partie() {
        mCartes = new Vector<>();
        mMain = new Vector<>(8);
        mPile_98 = new Vector<>(2);
        mPile_1 = new Vector<>(2);

        construireListes();
    }

    /**
     * Méthode qui construit la pile de cartes initiale, les deux piles augmentantes et
     * descendantes et qui brasse le paquet de cartes aléatoirement.
     */
    private void construireListes() {
        for (int i = 1; i <= 97; ++i) {
            mCartes.add(new Carte(i));
        }

        mPile_98.add("98");
        mPile_98.add("98");
        mPile_1.add("0");
        mPile_1.add("0");

        Collections.shuffle(mCartes);
    }

    /**
     * Méthode qui enlève la première carte du paquet de carte et retourne sa valeur (en String).
     * La valeur de la carte est ajoutée à la main de l'utilisateur
     *
     * @return String
     */
    public String piger() {
        Carte c = mCartes.get(0);
        mCartes.remove(0);

        String s = String.valueOf(c.getNombre());
        mMain.add(s);

        return s;
    }

    /**
     * Méthode retournant le nombre de cartes restantes dans le paquet de carte.
     *
     * @return int;
     */
    public int cartesRestantes() {
        return mCartes.size();
    }

    /**
     * Méthode qui vérifie si on peut placer une certaine carte sur la pile descendante.
     * Si oui, enlève la valeur de la carte de la main de l'utilisateur et remplace
     * la valeur de destination de la pile appropriée.
     *
     * @param valCarte
     * @param valDestination
     * @return boolean
     */
    public boolean diminuer(String valCarte, String valDestination) {
        boolean valide = false;
        int vCarte = Integer.parseInt(valCarte);
        int vDestination = Integer.parseInt(valDestination);

        if (vCarte < vDestination) {
            mMain.remove(valCarte);
            mPile_98.remove(valDestination);
            mPile_98.add(valCarte);
            valide = true;
        }

        else if (vCarte == vDestination + 10) {
            mMain.remove(valCarte);
            mPile_98.remove(valDestination);
            mPile_98.add(valCarte);
            valide = true;
        }

        return valide;
    }

    /**
     * Méthode qui vérifie si on peut placer une certaine carte sur la pile augmentante.
     * Si oui, enlève la valeur de la carte de la main de l'utilisateur et remplace
     * la valeur de destination de la pile appropriée.
     * @param valCarte
     * @param valDestination
     * @return boolean
     */
    public boolean augmenter(String valCarte, String valDestination) {
        boolean valide = false;
        int vCarte = Integer.parseInt(valCarte);
        int vDestination = Integer.parseInt(valDestination);

        if (vCarte > vDestination) {
            mMain.remove(valCarte);
            mPile_1.remove(valDestination);
            mPile_1.add(valCarte);
            valide = true;
        }

        else if (vCarte == vDestination - 10) {
            mMain.remove(valCarte);
            mPile_1.remove(valDestination);
            mPile_1.add(valCarte);
            valide = true;
        }

        return valide;
    }

    /**
     * Méthode vérifiant si la partie est toujours en cours.
     *
     * @return boolean
     */
    public boolean partieEnCours() {
        boolean valide = true;

        if (mMain.size() + mCartes.size() == 0) {
            valide = false;
        }

        return valide;
    }

    /**
     * Méthode vérifiant s'il y a au moins un déplacement valide dans la
     * partie en cours.
     *
     * @return boolean
     */
    public boolean deplacementPossible() {
        for (String val : mMain) {
            int valCarte = Integer.parseInt(val);

            for (String v_98 : mPile_98) {
                int val_98 = Integer.parseInt(v_98);

                if (valCarte < val_98) {
                    return true;
                }
            }

            for (String v_1 : mPile_1) {
                int val_1 = Integer.parseInt(v_1);

                if (valCarte > val_1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Méthode qui calcule et retourne le score du déplacement du joueur.
     *
     * @param debut
     * @param fin
     * @param score
     * @param valCarte
     * @param valDestination
     * @return int
     */
    public int changerScore(long debut, long fin, String score, String valCarte, String valDestination) {
        int valScore = Integer.parseInt(score);
        int vCarte = Integer.parseInt(valCarte);
        int vDestination = Integer.parseInt(valDestination);
        int temps = (int)(fin - debut) / 1000;

        if (temps <= 0) {
            temps = 1;
        }

        int temp = 0;

        // Modification du score selon la rapidité du déplacement
        int modTemps = 5 / temps;

        // Modification du score selon le nombre de cartes restant dans le paquet de cartes
        int modCartes = 10 - (mCartes.size() / 10);
        modTemps += modCartes;

        // Modification du score selon la proximité des valeurs de la carte et de sa destination
        temp += 97 / (Math.abs(vCarte - vDestination));
        temp *= modTemps;

        // Ajout des modifications au score actuel
        valScore += temp;

        return valScore;
    }
}
