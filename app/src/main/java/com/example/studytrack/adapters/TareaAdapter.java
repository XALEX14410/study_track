package com.example.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.models.Tarea;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Tarea tarea);
    }

    private final List<Tarea> tareas;
    private final OnItemClickListener listener;

    public TareaAdapter(List<Tarea> tareas) {
        this(tareas, null);
    }

    public TareaAdapter(List<Tarea> tareas, OnItemClickListener listener) {
        this.tareas = tareas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        holder.bind(tareas.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return tareas != null ? tareas.size() : 0;
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        private final TextView tituloTextView;
        private final TextView descripcionTextView;
        private final TextView fechaEntregaTextView;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloTextView = itemView.findViewById(R.id.tareaTituloTextView);
            descripcionTextView = itemView.findViewById(R.id.tareaDescripcionTextView);
            fechaEntregaTextView = itemView.findViewById(R.id.tareaFechaEntregaTextView);
        }

        // Separa el bind de datos para mantener ViewHolder limpio y reutilizable.
        void bind(Tarea tarea, OnItemClickListener listener) {
            tituloTextView.setText(tarea.getTitulo());
            descripcionTextView.setText(tarea.getDescripcion());
            fechaEntregaTextView.setText(tarea.getFechaEntrega());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(tarea);
                }
            });
        }
    }
}
