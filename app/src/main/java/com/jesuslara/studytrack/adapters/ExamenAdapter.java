package com.jesuslara.studytrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.models.Examen;

import java.util.List;

public class ExamenAdapter extends RecyclerView.Adapter<ExamenAdapter.ExamenViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Examen examen);
    }

    private final List<Examen> examenes;
    private final OnItemClickListener listener;

    public ExamenAdapter(List<Examen> examenes) {
        this(examenes, null);
    }

    public ExamenAdapter(List<Examen> examenes, OnItemClickListener listener) {
        this.examenes = examenes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_examen, parent, false);
        return new ExamenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamenViewHolder holder, int position) {
        holder.bind(examenes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return examenes != null ? examenes.size() : 0;
    }

    static class ExamenViewHolder extends RecyclerView.ViewHolder {

        private final TextView materiaTextView;
        private final TextView temaTextView;
        private final TextView fechaHoraTextView;

        ExamenViewHolder(@NonNull View itemView) {
            super(itemView);
            materiaTextView = itemView.findViewById(R.id.examenMateriaTextView);
            temaTextView = itemView.findViewById(R.id.examenTemaTextView);
            fechaHoraTextView = itemView.findViewById(R.id.examenFechaHoraTextView);
        }

        // Separa el bind de datos para mantener ViewHolder limpio y reutilizable.
        void bind(Examen examen, OnItemClickListener listener) {
            materiaTextView.setText(examen.getMateria());
            temaTextView.setText(examen.getTema());
            fechaHoraTextView.setText(examen.getFechaHora());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(examen);
                }
            });
        }
    }
}
