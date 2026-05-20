package com.example.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.models.QuickActionItem;

import java.util.List;

public class QuickActionsAdapter extends RecyclerView.Adapter<QuickActionsAdapter.QuickActionViewHolder> {

    public interface OnQuickActionClickListener {
        void onClick(QuickActionItem item);
    }

    private final List<QuickActionItem> items;
    private final OnQuickActionClickListener listener;

    public QuickActionsAdapter(List<QuickActionItem> items, OnQuickActionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuickActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_quick_action, parent, false);
        return new QuickActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickActionViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class QuickActionViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final TextView titleView;

        QuickActionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.quickActionIcon);
            titleView = itemView.findViewById(R.id.quickActionTitle);
        }

        void bind(QuickActionItem item, OnQuickActionClickListener listener) {
            iconView.setImageResource(item.getIconResId());
            titleView.setText(item.getTitle());
            itemView.setOnClickListener(view -> listener.onClick(item));
        }
    }
}
