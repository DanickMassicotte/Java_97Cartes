package com.example.a0582846.a97cartes;

import android.graphics.Color;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

/**
 * Classe représentant l'activité principale du jeu 97Cartes; c'est la
 * classe qui gère les interactions entre l'utilisateur et les cartes
 * et l'interface visuelle d'une partie en cours.
 *
 * @author Danick Massicotte
 * @version 1
 */
public class GameActivity extends AppCompatActivity {

    // Variables d'instances
    private TextView nbCartes;
    private TextView chrono;
    private TextView score;

    private LinearLayout cartes_haut;
    private LinearLayout cartes_bas;
    private ConstraintLayout piles;
    private LinearLayout pile98_1;
    private LinearLayout pile98_2;
    private LinearLayout pile1_1;
    private LinearLayout pile1_2;
    private ConstraintLayout cl;

    private Partie mPartie;
    private int mMainCourante;
    private DatabaseHelper db;
    private Vector<TextView> mCartesJetees;
    private Ecouteur ec;

    private boolean partieDebutee = false;
    private long debutPartie = 0;
    private long tempsDeplacement = 0;

    // Chronomètre de la partie en cours
    private Handler tempsHandler = new Handler();
    private Runnable tempsRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - debutPartie;
            int sec = (int)(millis / 1000);

            chrono.setText(String.valueOf(sec));
            tempsHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        nbCartes = findViewById(R.id.txt_nbCartes);
        chrono = findViewById(R.id.txt_Temps);
        score = findViewById(R.id.txt_Score);

        cartes_haut = findViewById(R.id.layout_cartesHaut);
        cartes_bas = findViewById(R.id.layout_cartesBas);
        piles = findViewById(R.id.layout_piles);
        pile98_1 = findViewById(R.id.pile98_1);
        pile98_2 = findViewById(R.id.pile98_2);
        pile1_1 = findViewById(R.id.pile1_1);
        pile1_2 = findViewById(R.id.pile1_2);

        mPartie = new Partie();
        mMainCourante = 8;
        db = DatabaseHelper.getInstance(this);
        mCartesJetees = new Vector<>(2);

        ec = new Ecouteur();

        // Donne une valeur à chaque TextView (carte) des layouts cartes_haut et cartes_bas ainsi
        // qu'attribut les écouteurs appropriés aux TextViews et LinearLayouts.
        for (int i = 0; i < cartes_haut.getChildCount(); ++i) {
            LinearLayout layout_haut = (LinearLayout)cartes_haut.getChildAt(i);
            TextView carte_haut = (TextView)layout_haut.getChildAt(0);
            carte_haut.setText(mPartie.piger());

            LinearLayout layout_bas = (LinearLayout)cartes_bas.getChildAt(i);
            TextView carte_bas = (TextView)layout_bas.getChildAt(0);
            carte_bas.setText(mPartie.piger());

            LinearLayout layout_pile = (LinearLayout)piles.getChildAt(i);

            layout_haut.setOnDragListener(ec);
            carte_haut.setOnTouchListener(ec);
            layout_bas.setOnDragListener(ec);
            carte_bas.setOnTouchListener(ec);
            layout_pile.setOnDragListener(ec);
        }

        // Nombre de cartes restantes dans le paquet de cartes.
        nbCartes.setText(String.valueOf(mPartie.cartesRestantes()));
    }

    /**
     * Classe écouteur pour gérer les événements "Drag and Drop".
     */
    private class Ecouteur implements View.OnDragListener, View.OnTouchListener {

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            switch(dragEvent.getAction()) {
                case DragEvent.ACTION_DROP:
                    TextView carte = (TextView)dragEvent.getLocalState();
                    LinearLayout parent = (LinearLayout)carte.getParent();
                    LinearLayout destination = (LinearLayout)view;
                    TextView enlever = (TextView)destination.getChildAt(0);     // Le TextView à remplacer par la carte

                    // Si enlever != null pour empêcher de mettre une carte sur
                    // un LinearLayout qui n'est pas une des quatres piles destinations
                    if (enlever != null) {
                        String valCarte = carte.getText().toString();
                        String valDestination = enlever.getText().toString();
                        boolean verification = false;

                        // Vérifie si le déplacement est valide
                        if (destination == pile98_1 || destination == pile98_2) {
                            verification = mPartie.diminuer(valCarte, valDestination);
                        }

                        else if (destination == pile1_1 || destination == pile1_2) {
                            verification = mPartie.augmenter(valCarte, valDestination);
                        }

                        // Enlève le TextView à la destination voulue et transfert la carte sélectionnée
                        // de son parent à sa destination.
                        if (verification) {
                            parent.removeView(carte);
                            destination.removeView(enlever);
                            destination.addView(carte);
                            carte.setVisibility(View.VISIBLE);
                            carte.setOnTouchListener(null);

                            // Recyclage de cartes enlevées
                            mCartesJetees.add(enlever);

                            // Mise à jour de l'affichage du score
                            int scr = mPartie.changerScore(tempsDeplacement, System.currentTimeMillis(), score.getText().toString(),
                                    carte.getText().toString(), enlever.getText().toString());
                            score.setText(String.valueOf(scr));

                            mMainCourante--;
                        }

                        // Remplir les cartes disponibles lorsqu'il y en manque deux
                        if (mMainCourante == 6) {
                            remplirMain();
                        }
                    }

                    // Vérifie si la partie est terminée
                    if(!mPartie.partieEnCours() || !mPartie.deplacementPossible()) {
                        finDePartie();
                    }

                    // Mise à jour de l'affichage des cartes restantes dans le paquet
                    nbCartes.setText(String.valueOf(mPartie.cartesRestantes()));
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    // Empêche de "perdre" les cartes qui ne sont pas déposées au bon endroit
                    TextView carteEnded = (TextView)dragEvent.getLocalState();
                    carteEnded.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }

            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            // Part le chronomètre de la partie
            if (!partieDebutee) {
                debutPartie = System.currentTimeMillis();
                tempsHandler.postDelayed(tempsRunnable, 0);

                partieDebutee = true;
            }

            // Temps au début d'un déplacement
            tempsDeplacement = System.currentTimeMillis();

            // Ombre de la carte déplacée
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);

            return true;
        }
    }

    /**
     * Méthode qui remplie la main de l'utilisateur des cartes manquantes dans celle-ci.
     * Pour chaque colonne des cartes haut et bas de la main, vérifie si l'espace est vide
     * (ne contient pas déjà une carte) et qu'on a assez de cartes dans le paquet pour piger et
     * recycle un TextView jeté précédemment pour en créer une "nouvelle" carte. Assigne les
     * propriétés et écouteur appropriés à la carte.
     */
    private void remplirMain() {
        if (mPartie.cartesRestantes() > 0) {
            for (int i = 0; i < cartes_haut.getChildCount(); ++i) {
                LinearLayout layout_haut = (LinearLayout)cartes_haut.getChildAt(i);
                TextView carte_haut = (TextView)layout_haut.getChildAt(0);

                LinearLayout layout_bas = (LinearLayout)cartes_bas.getChildAt(i);
                TextView carte_bas = (TextView)layout_bas.getChildAt(0);

                if (carte_haut == null && mPartie.cartesRestantes() > 0) {
                    TextView carte = mCartesJetees.elementAt(0);

                    mCartesJetees.remove(carte);
                    carte.setText(mPartie.piger());
                    carte.setBackgroundColor(getResources().getColor(R.color.cartesBG));
                    carte.setTextColor(Color.BLACK);
                    carte.setOnTouchListener(ec);
                    layout_haut.addView(carte);

                    ++mMainCourante;
                }

                if (carte_bas == null && mPartie.cartesRestantes() > 0) {
                    TextView carte = mCartesJetees.elementAt(0);

                    mCartesJetees.remove(carte);
                    carte.setText(mPartie.piger());
                    carte.setBackgroundColor(getResources().getColor(R.color.cartesBG));
                    carte.setTextColor(Color.BLACK);
                    carte.setOnTouchListener(ec);
                    layout_bas.addView(carte);

                    ++mMainCourante;
                }
            }
        }
    }

    /**
     * Méthode de fin de partie. Arrête le chronomètre, ajoute le score de l'utilisateur dans
     * la base de données et affiche un message indiquant que la partie est terminée.
     */
    private void finDePartie() {
        tempsHandler.removeCallbacks(tempsRunnable);
        db.ajouterScore(Integer.parseInt(score.getText().toString()));

        CharSequence texte = "Fin de la partie!";
        int duree = Toast.LENGTH_SHORT;

        Toast t = Toast.makeText(this, texte, duree);
        t.show();
    }
}
