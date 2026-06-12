package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Eğer butonların ImageButton ise ekle
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final List<PantryItem> pantryList;
    private final Context context;
    private OnItemActionListener listener;

    public PantryAdapter(Context context, List<PantryItem> pantryList) {
        this.context = context;
        this.pantryList = pantryList;
    }

    public void setListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pantry_ingredient, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(item.getQuantity() + " " + item.getUnit());

        SharedPreferences sharedPrefs = context.getSharedPreferences("PantryExpiryPrefs", Context.MODE_PRIVATE);
        String expiryDateStr = sharedPrefs.getString(item.getName() + "_expiry", null);

        if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date expiryDate = dateFormat.parse(expiryDateStr);
                Date today = new Date();

                if (expiryDate != null) {
                    long diffInMillis = expiryDate.getTime() - today.getTime();
                    long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                    if (diffInDays <= 0) {
                        holder.tvItemExpiryAlert.setText("Expired!");
                        holder.tvItemExpiryAlert.setTextColor(Color.parseColor("#BA1A1A"));
                        holder.tvItemExpiryAlert.setVisibility(View.VISIBLE);
                    } else if (diffInDays <= 3) {
                        holder.tvItemExpiryAlert.setText("Exp. in " + diffInDays + " days");
                        holder.tvItemExpiryAlert.setTextColor(Color.parseColor("#D32F2F"));
                        holder.tvItemExpiryAlert.setVisibility(View.VISIBLE);
                    } else {
                        holder.tvItemExpiryAlert.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                holder.tvItemExpiryAlert.setVisibility(View.GONE);
            }
        } else {
            holder.tvItemExpiryAlert.setVisibility(View.GONE);
        }

        if (listener != null) {
            if (holder.btnIncrease != null) {
                holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(holder.getAdapterPosition()));
            }
            if (holder.btnDecrease != null) {
                holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(holder.getAdapterPosition()));
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return pantryList != null ? pantryList.size() : 0;
    }

    public interface OnItemActionListener {
        void onDelete(int position);
        void onIncrease(int position);
        void onDecrease(int position);
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvItemExpiryAlert;
        View btnIncrease, btnDecrease, btnDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemExpiryAlert = itemView.findViewById(R.id.tvItemExpiryAlert);

            btnIncrease = itemView.findViewById(R.id.btnPlus);
            btnDecrease = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}