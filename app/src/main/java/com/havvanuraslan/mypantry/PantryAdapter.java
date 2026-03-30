package com.havvanuraslan.mypantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private List<PantryItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDelete(int position);
        void onIncrease(int position);
        void onDecrease(int position);
    }

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
        holder.btnPlus.setOnClickListener(v -> listener.onIncrease(holder.getAdapterPosition()));
        holder.btnMinus.setOnClickListener(v -> listener.onDecrease(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        ImageButton btnMinus, btnPlus, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}