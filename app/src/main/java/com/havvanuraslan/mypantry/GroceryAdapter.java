package com.havvanuraslan.mypantry;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ViewHolder> {

    private List<GroceryItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDelete(int position);
        void onCheckChanged(int position, boolean isChecked);
    }

    public GroceryAdapter(List<GroceryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.tvName.setText(item.getName());

        holder.cbBought.setOnCheckedChangeListener(null);
        holder.cbBought.setChecked(item.isChecked());

        if (item.isChecked()) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setAlpha(0.5f);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvName.setAlpha(1.0f);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));

        holder.cbBought.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onCheckChanged(holder.getAdapterPosition(), isChecked);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbBought;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            cbBought = itemView.findViewById(R.id.cbBought);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}