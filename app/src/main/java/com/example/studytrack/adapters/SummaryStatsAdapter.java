package com.example.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.models.SummaryStatItem;

import java.util.List;

public class SummaryStatsAdapter extends RecyclerView.Adapter<SummaryStatsAdapter.SummaryViewHolder> {

    private final List<SummaryStatItem> items;

    public SummaryStatsAdapter(List<SummaryStatItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_stat, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final TextView valueView;
        private final TextView titleView;

        SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.statIcon);
            valueView = itemView.findViewById(R.id.statValue);
            titleView = itemView.findViewById(R.id.statTitle);
        }

        void bind(SummaryStatItem item) {
            iconView.setImageResource(item.getIconResId());
            valueView.setText(item.getValue());
            titleView.setText(item.getTitle());
        }
    }
}
