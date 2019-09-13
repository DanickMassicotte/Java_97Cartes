package com.example.a0582846.a97cartes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Classe représentant l'activité de départ du jeu 97Cartes; représente la page
 * d'accueil de l'application.
 *
 * @author Danick Massicotte
 * @version 1
 */
public class WelcomeActivity extends AppCompatActivity {

    private TextView bestPoints;
    private Button btn_jouer;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bestPoints = findViewById(R.id.txt_bestPoints);
        btn_jouer = findViewById(R.id.btn_Jouer);
        db = DatabaseHelper.getInstance(this);

        // Affiche le meilleur score à date de l'utilisateur
        bestPoints.setText(db.meilleurScore());

        Ecouteur ec = new Ecouteur();
        btn_jouer.setOnClickListener(ec);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 333) {
            // Affiche le meilleur score à date de l'utilisateur
            bestPoints.setText(db.meilleurScore());
        }
    }

    /**
     * Classe écouteur qui gère les événements "onClick" du bouton
     */
    private class Ecouteur implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent i = new Intent(WelcomeActivity.this, GameActivity.class);
            startActivityForResult(i, 333);
        }
    }
}
