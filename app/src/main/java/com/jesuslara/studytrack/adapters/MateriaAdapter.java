package com.jesuslara.studytrack.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.models.Materia;

import java.util.ArrayList;
import java.util.List;

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder> {

    private static final String TAG = "MATERIAS";

    public interface OnItemClickListener {
        void onItemClick(Materia materia);
    }

    private final List<Materia> materias;
    private final OnItemClickListener listener;

    public MateriaAdapter(List<Materia> materias) {
        this(materias, null);
    }

    public MateriaAdapter(List<Materia> materias, OnItemClickListener listener) {
        this.materias = new ArrayList<>();
        this.listener = listener;
        updateItems(materias);
    }

    public void updateItems(@Nullable List<Materia> nuevasMaterias) {
        materias.clear();
        if (nuevasMaterias != null) {
            materias.addAll(nuevasMaterias);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new MateriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        try {
            Materia materia = (position >= 0 && position < materias.size())
                    ? materias.get(position)
                    : null;
            holder.bind(materia, listener);
        } catch (Exception exception) {
            Log.e(TAG, "Error mostrando item de materia", exception);
            holder.bind(null, listener);
        }
    }

    @Override
    public int getItemCount() {
        return materias != null ? materias.size() : 0;
    }

    static class MateriaViewHolder extends RecyclerView.ViewHolder {

        private final TextView nombreTextView;
        private final TextView estadoTextView;
        private final TextView profesorTextView;
        private final TextView aulaTextView;
        private final TextView countsTextView;

        MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.materiaNombreTextView);
            estadoTextView = itemView.findViewById(R.id.materiaEstadoTextView);
            profesorTextView = itemView.findViewById(R.id.materiaProfesorTextView);
            aulaTextView = itemView.findViewById(R.id.materiaAulaTextView);
            countsTextView = itemView.findViewById(R.id.materiaCountsTextView);
        }

        void bind(@Nullable Materia materia, OnItemClickListener listener) {
            Context context = itemView.getContext();

            if (materia == null) {
                bindFallback(context);
                itemView.setOnClickListener(null);
                return;
            }

            String nombre = materia.getNombre();
            nombreTextView.setText(isEmpty(nombre)
                    ? context.getString(R.string.detail_value_empty)
                    : nombre);

            String profesor = materia.getProfesor();
            profesorTextView.setText(context.getString(
                    R.string.materia_profesor_format,
                    isEmpty(profesor) ? context.getString(R.string.detail_value_empty) : profesor));

            String aula = materia.getAula();
            aulaTextView.setText(context.getString(
                    R.string.materia_aula_format,
                    isEmpty(aula) ? context.getString(R.string.detail_value_empty) : aula));

            int examenes = materia.getExamenes() != null ? materia.getExamenes().size() : 0;
            int tareas = materia.getTareas() != null ? materia.getTareas().size() : 0;
            countsTextView.setText(context.getString(
                    R.string.materia_counts_format, examenes, tareas));

            boolean activa = materia.isActiva();
            estadoTextView.setText(context.getString(
                    activa ? R.string.materia_estado_activa : R.string.materia_estado_inactiva));
            if (estadoTextView.getBackground() != null) {
                estadoTextView.setBackgroundTintList(ContextCompat.getColorStateList(
                        context,
                        activa ? R.color.st_priority_low : R.color.st_text_muted));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(materia);
                }
            });
        }

        private void bindFallback(Context context) {
            String fallback = context.getString(R.string.detail_value_empty);
            nombreTextView.setText(fallback);
            estadoTextView.setText(context.getString(R.string.materia_estado_inactiva));
            profesorTextView.setText(context.getString(R.string.materia_profesor_format, fallback));
            aulaTextView.setText(context.getString(R.string.materia_aula_format, fallback));
            countsTextView.setText(context.getString(R.string.materia_counts_format, 0, 0));
            if (estadoTextView.getBackground() != null) {
                estadoTextView.setBackgroundTintList(ContextCompat.getColorStateList(
                        context,
                        R.color.st_text_muted));
            }
        }

        private boolean isEmpty(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
}
