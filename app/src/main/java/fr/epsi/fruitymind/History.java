package fr.epsi.fruitymind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/*============================================================================================*//**
 * Gere l'historique des essais
 *//*=============================================================================================*/
public class History extends RecyclerView.Adapter<History.ItemView>
{
    private List<HistoryItem> m_items;  // Liste des entrees de l'historique

    /*========================================================================================*//**
     * Constructeur
     *//*=========================================================================================*/
    public History(LinearLayoutManager lm)
    {
        m_items = new ArrayList<HistoryItem>();

        // Auto scroll a l'ajout d'un item
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                lm.scrollToPosition(m_items.size() - 1);
            }
        });
    }

    /*========================================================================================*//**
     * Ajoute une entree a l'historique
     *
     * @param item item a ajouter
     *//*=========================================================================================*/
    public void addItem(HistoryItem item)
    {
        m_items.add(item);
        notifyItemInserted(m_items.size() - 1);
    }

    /*========================================================================================*//**
     * Efface l'historique
     *//*=========================================================================================*/
    public void clear()
    {
        m_items.clear();
        notifyDataSetChanged();
    }

    /*========================================================================================*//**
     * Renvoie le nombre d'entrees dans l'historique
     *//*=========================================================================================*/
    @Override
    public int getItemCount()
    {
        return m_items.size();
    }

    /*========================================================================================*//**
     * Appele a la creation d'un item
     *//*=========================================================================================*/
    @NonNull
    @Override
    public ItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View           view     = inflater.inflate(R.layout.history_item, parent, false);
        return new ItemView(view);
    }

    /*========================================================================================*//**
     * Lie les donnees a la vue de l'item
     *//*=========================================================================================*/
    @Override
    public void onBindViewHolder(@NonNull History.ItemView holder, int position)
    {
        int eval = m_items.get(position).getEval();
        holder.fruit_0.setImageDrawable(m_items.get(position).getFruitImage(0));
        holder.fruit_1.setImageDrawable(m_items.get(position).getFruitImage(1));
        holder.fruit_2.setImageDrawable(m_items.get(position).getFruitImage(2));
        holder.fruit_3.setImageDrawable(m_items.get(position).getFruitImage(3));
        holder.evaluation.setEvaluation((int)(eval / 10), eval % 10);
    }

    /*========================================================================================*//**
     * Decrit les elements visuels d'une entree de l'historique
     *//*=========================================================================================*/
    class ItemView extends RecyclerView.ViewHolder
    {
        public ImageView fruit_0;       // Premier fruit du code
        public ImageView fruit_1;       // Deuxieme fruit du code
        public ImageView fruit_2;       // Troisieme fruit du code
        public ImageView fruit_3;       // Quatrieme fruit du code
        public EvalView  evaluation;    // Vue de l'evaluation de l'essai

        /*====================================================================================*//**
         * Constructeur
         *//*=====================================================================================*/
        public ItemView(@NonNull View itemView)
        {
            super(itemView);

            fruit_0 = itemView.findViewById(R.id.fruit_0);
            fruit_1 = itemView.findViewById(R.id.fruit_1);
            fruit_2 = itemView.findViewById(R.id.fruit_2);
            fruit_3 = itemView.findViewById(R.id.fruit_3);
            evaluation = itemView.findViewById(R.id.evaluation);
        }
    }
}
