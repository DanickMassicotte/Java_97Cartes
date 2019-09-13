package com.example.a0582846.a97cartes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Singleton de la base de données servant à sauvegarder et récupérer
 * les meilleurs scores fait par l'utilisateur au jeu 97Cartes.
 *
 * @author Danick Massicotte
 * @version 1
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Variables d'instances
    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    /**
     * Retourne l'instance du Singleton de la base de données
     *
     * @param context
     * @return instance
     */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }

    // Méthode constructeur
    private DatabaseHelper(Context context) {
        super(context, "db", null, 1);
        ouvrirBD();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tpFinal(_id INTEGER PRIMARY KEY AUTOINCREMENT, score INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DELETE FROM tpFinal");
        onCreate(db);
    }

    // Méthodes
    /**
     * Ouvre la base de données. Utilisé par DatabaseHelper seulement.
     */
    private void ouvrirBD() {
        database = this.getWritableDatabase();
    }

    /**
     * Ferme la base de données. Utilisé par DatabaseHelper seulement.
     */
    private void fermerBD() {
        database.close();
    }

    /**
     * Méthode permettant d'ajouter un nouveau score dans la base de
     * données. S'ouvre elle-même dans le cas où elle est déjà fermée
     * et se ferme après l'inscription du score.
     *
     * @param score
     */
    public void ajouterScore(int score) {
        ouvrirBD();

        ContentValues cv = new ContentValues();
        cv.put("score", score);

        database.insert("tpFinal", null, cv);

        fermerBD();
    }

    /**
     * Méthode retournant le meilleur score inscrit à la base de données.
     * S'ouvre elle-même dans le cas où elle est déjà fermée
     * et se ferme après la transmission du meilleur score.
     *
     * @return score
     */
    public String meilleurScore() {
        ouvrirBD();

        String score = "0";
        Cursor cur = database.rawQuery("SELECT score FROM tpFINAL ORDER BY score DESC", null);

        if (cur.moveToFirst()) {
            score = String.valueOf(cur.getInt(0));
        }

        fermerBD();
        return score;
    }
}
