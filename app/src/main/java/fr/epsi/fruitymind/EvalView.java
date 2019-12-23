package fr.epsi.fruitymind;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class EvalView extends View
{
    private EvalCircle[] m_circles;

    public EvalView(Context context)
    {
        this(context, null);
    }

    public EvalView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public EvalView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public EvalView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);

        m_circles = new EvalCircle[4];
        for (int i = 0; i < 4; ++i)
            m_circles[i] = new EvalCircle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        int   size;         // Cote du carre contenant les "pions" du score
        float offx;         // Offset x du carre
        float offy;         // Offset y du carre
        float quartersize;  // Quart du cote du carre
        float radius;       // Rayon des cercles

        if (w > h)
        {
            size = w;
            offy = 0;
            offx = (w - size) / 2;
        }
        else
        {
            size = h;
            offy = (h - size) / 2;
            offx = 0;
        }

        quartersize = size / 4;
        radius = quartersize * 0.65f;

        m_circles[0].x = offx + 1 * quartersize + quartersize / 4;
        m_circles[0].y = offy + 1 * quartersize + quartersize / 4;
        m_circles[1].x = offx + 3 * quartersize - quartersize / 4;
        m_circles[1].y = offy + 1 * quartersize + quartersize / 4;
        m_circles[2].x = offx + 1 * quartersize + quartersize / 4;
        m_circles[2].y = offy + 3 * quartersize - quartersize / 4;
        m_circles[3].x = offx + 3 * quartersize - quartersize / 4;
        m_circles[3].y = offy + 3 * quartersize - quartersize / 4;

        m_circles[0].radius = radius;
        m_circles[1].radius = radius;
        m_circles[2].radius = radius;
        m_circles[3].radius = radius;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        for (int i = 0; i < 4; ++i)
            m_circles[i].draw(canvas);
    }

    public void setEvaluation(int nplaced, int nmisplaced)
    {
        int i = 0;
        while (nplaced-- > 0)
            m_circles[i++].setScore(0);
        while (nmisplaced-- > 0)
            m_circles[i++].setScore(1);
        while (i < 4)
            m_circles[i++].setScore(2);
        invalidate();
    }

    class EvalCircle
    {
        public  float x;
        public  float y;
        public  float radius;
        private Paint m_paintInner;

        public EvalCircle()
        {
            m_paintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        void setScore(int i)
        {
            if (i == 0)
                m_paintInner.setColor(0xBB00BB00);
            else if (i == 1)
                m_paintInner.setColor(0xBBBB4400);
            else
                m_paintInner.setColor(0);

        }

        void draw(Canvas canvas)
        {
            canvas.drawCircle(x, y, radius, m_paintInner);
        }
    }
}
