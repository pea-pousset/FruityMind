package fr.epsi.fruitymind;

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class History extends RecyclerView.Adapter<History.ItemView>
{
    private List<HistoryItem> m_items;

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

    public void addItem(HistoryItem item)
    {
        m_items.add(item);
        notifyItemInserted(m_items.size() - 1);
    }

    public void clear()
    {
        m_items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return m_items.size();
    }

    @NonNull
    @Override
    public ItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View           view     = inflater.inflate(R.layout.history_item, parent, false);
        return new ItemView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull History.ItemView holder, int position)
    {
        int eval = m_items.get(position).getEval();
        Log.d("SCORE", Integer.toString(eval));
        holder.fruit_0.setImageDrawable(m_items.get(position).getFruitImage(0));
        holder.fruit_1.setImageDrawable(m_items.get(position).getFruitImage(1));
        holder.fruit_2.setImageDrawable(m_items.get(position).getFruitImage(2));
        holder.fruit_3.setImageDrawable(m_items.get(position).getFruitImage(3));
        holder.evaluation.setEvaluation((int)(eval / 10), eval % 10);
    }

    class ItemView extends RecyclerView.ViewHolder
    {
        public ImageView fruit_0;
        public ImageView fruit_1;
        public ImageView fruit_2;
        public ImageView fruit_3;
        public EvalView  evaluation;

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
