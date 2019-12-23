package fr.epsi.fruitymind;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Arrays;

/*============================================================================================*//**
 *
 *//*=============================================================================================*/
public class HistoryItem
{
    private Drawable[] m_fruitImages;
    private int        m_eval;

    public HistoryItem(Resources res, int[] userInput, int nplaced, int nmisplaced)
    {
        m_fruitImages = new Drawable[MainActivity.CODE_LEN];
        m_eval = nplaced * 10 + nmisplaced;
        for (int i = 0; i < MainActivity.CODE_LEN; ++i)
            m_fruitImages[i] = res.obtainTypedArray(R.array.fruitImages).getDrawable(userInput[i]);
    }

    public Drawable getFruitImage(int index)
    {
        if (index < 0 || index >= MainActivity.CODE_LEN)
            return null;
        return m_fruitImages[index];
    }

    public int getEval()
    {
        return m_eval;
    }
}
