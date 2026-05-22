package com.example.studytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.example.studytrack.adapters.TareaAdapter;
import com.example.studytrack.models.Tarea;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TareasFragment extends Fragment {

    private RecyclerView tareasRecyclerView;
    private FloatingActionButton tareasFabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tareas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tareasRecyclerView = view.findViewById(R.id.tareasRecyclerView);
        tareasFabAdd = view.findViewById(R.id.tareasFabAdd);

        // Configuracion base del RecyclerView.
        tareasRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tareasRecyclerView.setHasFixedSize(true);

        // Datos de prueba hardcoded para validar el flujo visual y funcional.
        List<Tarea> tareasDemo = new ArrayList<>();
        tareasDemo.add(new Tarea(
            "Resolver guia de algebra",
            "Completar ejercicios 1 al 20 del capitulo 3.",
            "Entrega: 24/05/2026"
        ));
        tareasDemo.add(new Tarea(
            "Resumen de historia",
            "Preparar resumen sobre la Revolucion Francesa.",
            "Entrega: 26/05/2026"
        ));
        tareasDemo.add(new Tarea(
            "Laboratorio de programacion",
            "Implementar lista enlazada simple en Java.",
            "Entrega: 28/05/2026"
        ));

        TareaAdapter tareaAdapter = new TareaAdapter(tareasDemo, tarea -> {
            Bundle bundle = new Bundle();
            bundle.putString("titulo", tarea.getTitulo());
            bundle.putString("descripcion", tarea.getDescripcion());
            bundle.putString("fechaEntrega", tarea.getFechaEntrega());

            Navigation.findNavController(view).navigate(
                R.id.action_nav_tareas_to_detalleTareaFragment,
                bundle
            );
        });
        tareasRecyclerView.setAdapter(tareaAdapter);

        tareasFabAdd.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                getString(R.string.toast_add_task),
                Toast.LENGTH_SHORT
        ).show());
    }
}