package fr.epsi.fruitymind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*============================================================================================*//**
 * Classe principale de l'application
 *//*=============================================================================================*/
public class MainActivity extends AppCompatActivity
{
    static final int CODE_LEN  = 4;     // Nombre de fruits dans un code
    static final int MAX_TRIES = 10;    // Nombre maximum d'essais autorise

    private int         m_numFruits;    // Nombre de fruits differents
    private boolean[]   m_withSeed;     // Attribut withSeed pour chaque fruit
    private boolean[]   m_peelable;     // Attribut peelable pour chaque fruit
    private int[]       m_iFruits;      // Liste d'indices des fruits melangee
    private int[]       m_userEntry;    // Indices des fruits choisis par le joueur
    private int         m_score;        // Score total
    private int         m_numTries;     // Nombre d'essais joues dans le set en cours
    private int         m_numGames;     // Nombre de parties gagnees
    private boolean     m_seedHintUsed; // Indice graine utlise
    private boolean     m_peelHintUsed; // Indice pelable utilise
    private History     m_history;      // Historique des essais joues
    private Resources   res;            // Ressources de l'application
    private ImageView[] m_seedIcons;
    private ImageView[] m_peelIcons;

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
        findViewById(R.id.validateButton).setOnClickListener(v -> makeGuess());

        // Met en place l'historique des essais
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

        // Recupere les icones d'indices
        m_seedIcons = new ImageView[CODE_LEN];
        m_peelIcons = new ImageView[CODE_LEN];
        m_seedIcons[0] = findViewById(R.id.seed_0);
        m_seedIcons[1] = findViewById(R.id.seed_1);
        m_seedIcons[2] = findViewById(R.id.seed_2);
        m_seedIcons[3] = findViewById(R.id.seed_3);
        m_peelIcons[0] = findViewById(R.id.peel_0);
        m_peelIcons[1] = findViewById(R.id.peel_1);
        m_peelIcons[2] = findViewById(R.id.peel_2);
        m_peelIcons[3] = findViewById(R.id.peel_3);


        m_userEntry = new int[CODE_LEN];
        newGame();
    }

    /*========================================================================================*//**
     * Cree le menu principal
     *//*=========================================================================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*========================================================================================*//**
     * Affichage du menu
     *//*=========================================================================================*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        // Grise les indices indisponibles
        menu.getItem(0).setEnabled(!m_seedHintUsed);
        menu.getItem(1).setEnabled(!m_peelHintUsed);
        return true;
    }

    /*========================================================================================*//**
     * Appele quand le joueur clique sur un item du menu
     *//*=========================================================================================*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.hintSeed)
        {
            if (m_seedHintUsed)
                return true;
            showSeedHint();
        }
        else if (item.getItemId() == R.id.hintPeel)
        {
            if (m_peelHintUsed)
                return true;
            showPeelHint();
        }

        return true;
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
        m_numGames = 0;
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
        m_peelHintUsed = false;
        m_history.clear();
        updateScoreDisplay();

        // Cache la vue des indices et met les icones a "blank"
        findViewById(R.id.hintsLayout).setVisibility(View.GONE);
        for (int i = 0; i < CODE_LEN; ++i)
        {
            m_seedIcons[i].setImageDrawable(res.getDrawable(R.drawable.blank));
            m_peelIcons[i].setImageDrawable(res.getDrawable(R.drawable.blank));
        }
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
     * Grise les entrees indice dans le menu en fonction du score
     *//*=========================================================================================*/
    private void updateHintsEnabled()
    {
        int usedHintsCount = 0;
        int hintCost = 0;

        if (m_seedHintUsed)
            ++usedHintsCount;
        if (m_peelHintUsed)
            ++usedHintsCount;

        if (usedHintsCount == 0)
            hintCost = 2;
        else
            hintCost = 3;

        if (hintCost > MAX_TRIES - m_numTries)
        {
            // On fait comme si les indices avaient ete utilises pour les griser
            m_seedHintUsed = true;
            m_peelHintUsed = true;
        }
    }

    /*========================================================================================*//**
     * Affiche les indices "a egrainer"
     *//*=========================================================================================*/
    void showSeedHint()
    {
        // On ne verifie pas si il reste assez d'essais, le menu est deja grise dans ce cas
        if (m_seedHintUsed)
            return;

        if (m_peelHintUsed) // Autre indice utilise : coup = 3 essais
        {
            m_numTries += 3;
            tryToast("-3");
        }
        else
        {
            m_numTries += 2;
            tryToast("-2");
        }

        m_seedHintUsed = true;
        findViewById(R.id.hintsLayout).setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; ++i)
        {
            if (m_withSeed[m_iFruits[i]])
                m_seedIcons[i].setImageDrawable(res.getDrawable(R.drawable.seed_true));
            else
                m_seedIcons[i].setImageDrawable(res.getDrawable(R.drawable.seed_false));

        }
        updateHintsEnabled();
        updateScoreDisplay();
    }

    /*========================================================================================*//**
     * Affiche les indices "a peler"
     *//*=========================================================================================*/
    void showPeelHint()
    {
        if (m_peelHintUsed)
            return;

        if (m_seedHintUsed)
        {
            m_numTries += 3;
            tryToast("-3");
        }
        else
        {
            m_numTries += 2;
            tryToast("-2");
        }

        m_peelHintUsed = true;
        findViewById(R.id.hintsLayout).setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; ++i)
        {
            if (m_peelable[m_iFruits[i]])
                m_peelIcons[i].setImageDrawable(res.getDrawable(R.drawable.peel_true));
            else
                m_peelIcons[i].setImageDrawable(res.getDrawable(R.drawable.peel_false));
        }
        updateHintsEnabled();
        updateScoreDisplay();
    }

    /*========================================================================================*//**
     * Traite une tentative du joueur
     *//*=========================================================================================*/
    private void makeGuess()
    {
        int   placed      = 0;
        int   misplaced   = 0;
        int[] fruitsCount = new int[m_numFruits];

        // En bourrinant le bouton on peut continuer a l'actionner pendant l'animation
        // d'affichage du dialog modal...
        if (m_numTries >= MAX_TRIES)
            return;

        ++m_numTries;
        updateScoreDisplay();
        updateHintsEnabled();

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

        // Ajoute l'essai a l'historique
        m_history.addItem(new HistoryItem(res, m_userEntry, placed, misplaced));

        if (placed == CODE_LEN)
        {
            // 4 fruits bien places : gagne !
            scoreToast();

            // On a decremente le nombre d'essai puisque le coup est joue, mais on voudrait
            // 10 points pour un code trouve au premier essai et 1 au dixieme
            m_score += MAX_TRIES - m_numTries + 1;
            ++m_numGames;
            updateScoreDisplay();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(R.string.gameWonTitle);
            builder.setMessage(R.string.gameWonMessage);
            builder.setPositiveButton(R.string.continueButton, (d, w) -> newSet());
            builder.setNeutralButton(R.string.restartButton, (d, w) -> newGame());
            builder.setNegativeButton(R.string.quitButton, (d, w) -> finishAndRemoveTask());
            builder.show();
        }
        else if (m_numTries == MAX_TRIES)
        {
            // Nombre max d'essais atteint : perdu :(
            StringBuilder msg = new StringBuilder();
            msg.append(res.getString(R.string.numGamesWon));
            msg.append(m_numGames);
            msg.append("\n");
            msg.append(res.getString(R.string.score));
            msg.append(m_score);
            msg.append("\n");
            msg.append(res.getString(R.string.solution));
            for (int i = 0; i < CODE_LEN; ++i)
            {
                msg.append(res.getStringArray(R.array.fruitNames)[m_iFruits[i]]);
                if (i < CODE_LEN - 1)
                    msg.append(", ");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(R.string.gameLostTitle);
            builder.setMessage(msg.toString());
            builder.setPositiveButton(R.string.restartButton, (d, w) -> newGame());
            builder.setNegativeButton(R.string.quitButton, (d, w) -> finishAndRemoveTask());
            builder.show();
        }
        // Pas de else, le jeu continue
    }

    /*========================================================================================*//**
     * Affiche un toast "+n" points sur le score
     *//*=========================================================================================*/
    void scoreToast()
    {
        int scoreLoc[] = new int[2];
        String txt = String.format("+%d", MAX_TRIES - m_numTries+1);
        Toast toast =
            Toast.makeText(this, txt, Toast.LENGTH_LONG);

        // Trouve la position du score
        findViewById(R.id.score).getLocationOnScreen(scoreLoc);
        scoreLoc[1] -= findViewById(R.id.score).getHeight();
        toast.setGravity(Gravity.TOP | Gravity.LEFT, scoreLoc[0], scoreLoc[1]);
        toast.show();
    }

    /*========================================================================================*//**
     * Affiche un toast sur le nombre d'essais
     *
     * @param text Texte du toast
     *//*=========================================================================================*/
    void tryToast(String text)
    {
        int tryLoc[] = new int[2];
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);

        // Trouve la position du nombre d'essais
        findViewById(R.id.numTries).getLocationOnScreen(tryLoc);
        tryLoc[1] -= findViewById(R.id.score).getHeight();
        toast.setGravity(Gravity.TOP | Gravity.LEFT, tryLoc[0], tryLoc[1]);
        toast.show();
    }
}
