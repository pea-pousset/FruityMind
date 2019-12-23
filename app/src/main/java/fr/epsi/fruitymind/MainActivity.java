package fr.epsi.fruitymind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    static final int CODE_LEN  = 4;
    static final int MAX_TRIES = 10;

    private int       m_numFruits;  // Nombre de fruits differents
    private boolean[] m_withSeed;   // Attribut withSeed pour chaque fruit
    private boolean[] m_peelable;   // Attribut peelable pour chaque fruit
    private int[]     m_iFruits;    // Liste d'indices des fruits melangee a chaque nouvelle partie
    private int[]     m_userEntry;  // Indices des fruits choisis par le joueur
    private int       m_score;
    private int       m_numTries;
    private boolean   m_seedHintUsed;       // Indice graine utlise
    private boolean   m_peelableHintUsed;   // Indice pelable utilise
    private History   m_history;
    private Resources res;

    /*========================================================================================*//**
     * Initialisation de l'activite
     *//*=========================================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        res = getResources();

        // Met en place les menus
        registerForContextMenu(findViewById(R.id.userEntry_0));
        registerForContextMenu(findViewById(R.id.userEntry_1));
        registerForContextMenu(findViewById(R.id.userEntry_2));
        registerForContextMenu(findViewById(R.id.userEntry_3));
        // Met en place la fonction appelee en cas de clic du bouton unique
        findViewById(R.id.validateButton).setOnClickListener((View v) -> makeGuess());

        LinearLayoutManager lm = new LinearLayoutManager(this);
        m_history = new History(lm);
        ((RecyclerView)findViewById(R.id.history)).setAdapter(m_history);
        ((RecyclerView)findViewById(R.id.history)).setLayoutManager(lm);

        // Recupere le nombre de fruits defini dans fruits.xml et cree les tableaux d'attributs
        m_numFruits = getResources().getInteger(R.integer.numFruits);
        m_withSeed = new boolean[m_numFruits];
        m_peelable = new boolean[m_numFruits];
        m_iFruits = new int[m_numFruits];

        // Recupere les valeurs des attributs definis dans fruits.xml et initalise la liste
        // d'indices
        for (int i = 0; i < m_numFruits; ++i)
        {
            m_withSeed[i] = res.obtainTypedArray(R.array.fruitWithSeed).getBoolean(i, true);
            m_peelable[i] = res.obtainTypedArray(R.array.fruitPeelable).getBoolean(i, true);
            m_iFruits[i] = i;
        }

        m_userEntry = new int[CODE_LEN];

        newGame();
    }

    /*========================================================================================*//**
     * Lie le menu contextuel a une vue
     *//*=========================================================================================*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(res.getString(R.string.txtFruitChoice));
        for (int i = 0; i < m_numFruits; ++i)
        {
            // On cree le menu programmatiquement en s'assurant que l'ordre correspond a l'indice
            // des fruits
            menu.add(0, v.getId(), i, res.getStringArray(R.array.fruitNames)[i]);
        }
    }

    /*========================================================================================*//**
     * Appele quand le joueur a selectionne un fruit via le menu
     *//*=========================================================================================*/
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.userEntry_0: setEntry(0, item.getOrder()); break;
            case R.id.userEntry_1: setEntry(1, item.getOrder()); break;
            case R.id.userEntry_2: setEntry(2, item.getOrder()); break;
            case R.id.userEntry_3: setEntry(3, item.getOrder()); break;
        }
        return true;
    }

    /*========================================================================================*//**
     * Cree une nouvelle partie
     *//*=========================================================================================*/
    private void newGame()
    {
        m_score = 0;
        newSet();
    }

    /*========================================================================================*//**
     * Cree un nouveau code
     *//*=========================================================================================*/
    private void newSet()
    {
        // Melange le tableau des indices avec un fisher-yates, les 4 premiers forment le code
        for (int i = m_numFruits - 1; i > 0; --i)
        {
            int j   = (int) (Math.random() * (i + 1));
            int tmp = m_iFruits[i];
            m_iFruits[i] = m_iFruits[j];
            m_iFruits[j] = tmp;
        }

        // Remet le code du joueur a 0
        for (int i = 0; i < CODE_LEN; ++i)
            setEntry(i, 0);

        m_numTries = 0;
        m_seedHintUsed = false;
        m_peelableHintUsed = false;
        m_history.clear();
        updateScoreDisplay();
    }

    /*========================================================================================*//**
     * Met a jour le score et le nombre d'essai affiches
     *//*=========================================================================================*/
    private void updateScoreDisplay()
    {
        ((TextView) findViewById(R.id.score)).setText(Integer.toString(m_score));
        ((TextView) findViewById(R.id.numTries)).setText(Integer.toString(MAX_TRIES-m_numTries));
    }

    /*========================================================================================*//**
     * Met a jour le code (image et valeur) selon le choix du joueur
     *
     * @param iEntry place du fruit dans le code
     * @param iFruit indice du type de fruit selon fruits.xml
     *//*=========================================================================================*/
    private void setEntry(int iEntry, int iFruit)
    {
        ImageView iv = null;
        switch (iEntry)
        {
            case 0: iv = findViewById(R.id.userEntry_0); break;
            case 1: iv = findViewById(R.id.userEntry_1); break;
            case 2: iv = findViewById(R.id.userEntry_2); break;
            case 3: iv = findViewById(R.id.userEntry_3); break;
            default:
                // CRASH
                break;
        }
        iv.setImageDrawable(res.obtainTypedArray(R.array.fruitImages).getDrawable(iFruit));
        m_userEntry[iEntry] = iFruit;
    }

    /*========================================================================================*//**
     * Traite une tentative du joueur
     *//*=========================================================================================*/
    private void makeGuess()
    {
        int   placed      = 0;
        int   misplaced   = 0;
        int[] fruitsCount = new int[m_numFruits];

        ++m_numTries;
        updateScoreDisplay();

        // Le code secret ne peut pas contenir de doublons mais le code du joueur, si. On procede
        // donc comme si les doublons etaient possibles.
        // On compte le nombre d'exemplaire de chaque type de fruit present dans le code secret:
        for (int i = 0; i < m_numFruits; ++i)
            fruitsCount[i] = 0;
        for (int i = 0; i < CODE_LEN; ++i)
            ++fruitsCount[m_iFruits[i]];

        // Passe 1 : On compte les fruits bien places et on decremente le compteur des fruits
        // presents pour ne pas les reutiliser dans la passe 2
        for (int i = 0; i < CODE_LEN; ++i)
        {
            if (m_userEntry[i] == m_iFruits[i])
            {
                --fruitsCount[m_iFruits[i]];
                ++placed;
            }
        }

        // Passe 2 : On compte les fruits mal places si il reste des fruits non utilises par la
        // passe 1
        for (int i = 0; i < CODE_LEN; ++i)
        {
            if (m_userEntry[i] == m_iFruits[i])
                continue;

            for (int j = 0; j < CODE_LEN; ++j)
            {
                if ((m_userEntry[i] == m_iFruits[j]) && (fruitsCount[m_iFruits[j]] > 0))
                {
                    --fruitsCount[m_iFruits[j]];
                    ++misplaced;
                }
            }
        }

        Log.d("Code ", Arrays.toString(m_iFruits));
        Log.d("Guess", Arrays.toString(m_userEntry));
        Log.d("Bien place", Integer.toString(placed));
        Log.d("Mal place ", Integer.toString(misplaced));

        m_history.addItem(new HistoryItem(res, m_userEntry, placed, misplaced));

        // SI bien place = CODE_LEN : gagne
        // SINON si essai = MAX_TRIES : perdu
        // SINON continue
    }
}
