package fr.epsi.fruitymind;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/*============================================================================================*//**
 * Contient les donnees d'un essai a afficher dans l'historique
 *//*=============================================================================================*/
public class HistoryItem
{
    private Drawable[] m_fruitImages;   // Liste des fruits joues
    private int        m_eval;          // Evaluation de l'essai

    /*========================================================================================*//**
     * Constructeur
     *
     * @param res        resources de l'application contenant les images
     * @param userInput  indices des fruits joues
     * @param nplaced    nombre de fruits bien places
     * @param nmisplaced nombre de fruits mal places
     *//*=========================================================================================*/
    public HistoryItem(Resources res, int[] userInput, int nplaced, int nmisplaced)
    {
        m_fruitImages = new Drawable[MainActivity.CODE_LEN];
        m_eval = nplaced * 10 + nmisplaced;
        for (int i = 0; i < MainActivity.CODE_LEN; ++i)
            m_fruitImages[i] = res.obtainTypedArray(R.array.fruitImages).getDrawable(userInput[i]);
    }

    /*========================================================================================*//**
     * Renvoie l'image d'un fruit dans le code joue
     *
     * @param index position du fruit dans le code joue
     *//*=========================================================================================*/
    public Drawable getFruitImage(int index)
    {
        if (index < 0 || index >= MainActivity.CODE_LEN)
            return null;
        return m_fruitImages[index];
    }

    /*========================================================================================*//**
     * Renvoie un entier decrivant l'evaluation de l'essai : dizaines = fruits bien places,
     * unites = fruits mal places
     *//*=========================================================================================*/
    public int getEval()
    {
        return m_eval;
    }
}
