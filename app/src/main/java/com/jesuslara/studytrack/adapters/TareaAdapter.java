package com.jesuslara.studytrack.adapters;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.databinding.ItemTareaBinding;
import com.jesuslara.studytrack.models.Tarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private static final int MENU_EDITAR = 100;
    private static final int MENU_ELIMINAR = 101;

    public interface OnTareaActionListener {
        void onTareaClick(@NonNull Tarea tarea);

        void onEditarTarea(@NonNull Tarea tarea);

        void onEliminarTarea(@NonNull Tarea tarea);

        void onCompletadaChanged(@NonNull Tarea tarea, boolean completada, int position);
    }

    private final List<Tarea> tareas = new ArrayList<>();
    @Nullable
    private final OnTareaActionListener actionListener;

    public TareaAdapter(@Nullable OnTareaActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void updateItems(@Nullable List<Tarea> nuevasTareas) {
        List<Tarea> nuevas = nuevasTareas != null ? new ArrayList<>(nuevasTareas) : new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return tareas.size();
            }

            @Override
            public int getNewListSize() {
                return nuevas.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return isSameItemKey(tareas.get(oldItemPosition), nuevas.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return isSameContent(tareas.get(oldItemPosition), nuevas.get(newItemPosition));
            }
        });

        tareas.clear();
        tareas.addAll(nuevas);
        diffResult.dispatchUpdatesTo(this);
    }

    public void restoreItemCompletionState(@NonNull String tareaId, boolean completada) {
        for (int i = 0; i < tareas.size(); i++) {
            Tarea tarea = tareas.get(i);
            if (Objects.equals(tarea.getId(), tareaId)) {
                tarea.setCompletada(completada);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTareaBinding binding = ItemTareaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TareaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        holder.bind(tareas.get(position), actionListener);
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    private boolean isSameItemKey(@NonNull Tarea oldItem, @NonNull Tarea newItem) {
        if (!TextUtils.isEmpty(oldItem.getId()) && !TextUtils.isEmpty(newItem.getId())) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }
        return Objects.equals(oldItem.getNombre(), newItem.getNombre())
                && Objects.equals(oldItem.getFechaEntrega(), newItem.getFechaEntrega())
                && Objects.equals(oldItem.getHoraEntrega(), newItem.getHoraEntrega());
    }

    private boolean isSameContent(@NonNull Tarea oldItem, @NonNull Tarea newItem) {
        return Objects.equals(oldItem.getNombre(), newItem.getNombre())
                && Objects.equals(oldItem.getDescripcion(), newItem.getDescripcion())
                && Objects.equals(oldItem.getMateriaNombre(), newItem.getMateriaNombre())
                && Objects.equals(oldItem.getPrioridad(), newItem.getPrioridad())
                && Objects.equals(oldItem.getFechaEntrega(), newItem.getFechaEntrega())
                && Objects.equals(oldItem.getHoraEntrega(), newItem.getHoraEntrega())
                && oldItem.isCompletada() == newItem.isCompletada()
                && Objects.equals(oldItem.getUpdatedAt(), newItem.getUpdatedAt());
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        private final ItemTareaBinding binding;

        TareaViewHolder(@NonNull ItemTareaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull Tarea tarea, @Nullable OnTareaActionListener listener) {
            String nombre = safeText(
                    tarea.getNombre(),
                    itemView.getContext().getString(R.string.detail_value_empty)
            );
            String descripcion = safeText(
                    tarea.getDescripcion(),
                    itemView.getContext().getString(R.string.detail_value_empty)
            );
            String materia = safeText(
                    tarea.getMateriaNombre(),
                    itemView.getContext().getString(R.string.tarea_chip_materia_placeholder)
            );
            String prioridad = safeText(
                    tarea.getPrioridad(),
                    itemView.getContext().getString(R.string.tarea_chip_prioridad_placeholder)
            );

            binding.tvNombre.setText(nombre);
            binding.tvDescripcion.setText(descripcion);
            binding.chipMateria.setText(materia);
            binding.chipPrioridad.setText(prioridad);
            binding.tvFechaHora.setText(buildFechaHora(tarea));

            applyPriorityState(prioridad);

            binding.cbCompletada.setOnCheckedChangeListener(null);
            binding.cbCompletada.setChecked(tarea.isCompletada());
            applyCompletedState(tarea);

            binding.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener == null || tarea.isCompletada() == isChecked) {
                    return;
                }

                tarea.setCompletada(isChecked);
                applyCompletedState(tarea);

                int adapterPosition = getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onCompletadaChanged(tarea, isChecked, adapterPosition);
                }
            });

            binding.btnMenu.setOnClickListener(v -> showPopupMenu(v, tarea, listener));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTareaClick(tarea);
                }
            });
        }

        private void showPopupMenu(@NonNull View anchor,
                                   @NonNull Tarea tarea,
                                   @Nullable OnTareaActionListener listener) {
            PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, MENU_EDITAR, Menu.NONE, R.string.tarea_menu_editar);
            menu.add(Menu.NONE, MENU_ELIMINAR, Menu.NONE, R.string.tarea_menu_eliminar);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (listener == null) {
                    return true;
                }
                if (item.getItemId() == MENU_EDITAR) {
                    listener.onEditarTarea(tarea);
                    return true;
                }
                if (item.getItemId() == MENU_ELIMINAR) {
                    listener.onEliminarTarea(tarea);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }

        private String buildFechaHora(@NonNull Tarea tarea) {
            String fecha = tarea.getFechaEntrega();
            String hora = tarea.getHoraEntrega();

            if (TextUtils.isEmpty(fecha) && TextUtils.isEmpty(hora)) {
                return itemView.getContext().getString(R.string.tarea_fecha_hora_placeholder);
            }
            if (TextUtils.isEmpty(hora)) {
                return fecha;
            }
            if (TextUtils.isEmpty(fecha)) {
                return hora;
            }
            return fecha + " • " + hora;
        }

        private void applyCompletedState(@NonNull Tarea tarea) {
            boolean completada = tarea.isCompletada();

            int flags = binding.tvNombre.getPaintFlags();
            if (completada) {
                binding.tvNombre.setPaintFlags(flags | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvNombre.setAlpha(0.65f);
                binding.tvDescripcion.setAlpha(0.65f);
                binding.getRoot().setAlpha(0.82f);
                binding.getRoot().setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.st_success)
                );
                String fechaHora = buildFechaHora(tarea);
                binding.tvFechaHora.setText(itemView.getContext().getString(
                        R.string.tarea_estado_completada_format,
                        fechaHora
                ));
            } else {
                binding.tvNombre.setPaintFlags(flags & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvNombre.setAlpha(1f);
                binding.tvDescripcion.setAlpha(1f);
                binding.getRoot().setAlpha(1f);
                binding.getRoot().setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.st_outline)
                );
                binding.tvFechaHora.setText(buildFechaHora(tarea));
            }
        }

        private void applyPriorityState(@NonNull String prioridad) {
            String normalized = prioridad.toLowerCase(Locale.ROOT);
            int colorRes = R.color.st_priority_low;
            if (normalized.contains("alta")) {
                colorRes = R.color.st_priority_high;
            } else if (normalized.contains("media")) {
                colorRes = R.color.st_priority_medium;
            }

            binding.chipPrioridad.setChipBackgroundColorResource(colorRes);
            binding.chipPrioridad.setTextColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.white)
            );
            binding.chipPrioridad.setChipIconTintResource(R.color.white);
        }

        private String safeText(@Nullable String value, @NonNull String fallback) {
            return TextUtils.isEmpty(value) ? fallback : value;
        }
    }
}
