package com.example.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.models.UpcomingEventItem;

import java.util.List;

public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.EventViewHolder> {

    private final List<UpcomingEventItem> items;

    public UpcomingEventsAdapter(List<UpcomingEventItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        private final View priorityStripView;
        private final TextView subjectView;
        private final TextView dateView;
        private final TextView priorityView;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityStripView = itemView.findViewById(R.id.eventPriorityStrip);
            subjectView = itemView.findViewById(R.id.eventSubject);
            dateView = itemView.findViewById(R.id.eventDate);
            priorityView = itemView.findViewById(R.id.eventPriority);
        }

        void bind(UpcomingEventItem item) {
            priorityStripView.setBackgroundColor(item.getPriorityColor());
            subjectView.setText(item.getSubject());
            dateView.setText(item.getDate());
            priorityView.setText(item.getPriority());
        }
    }
}
