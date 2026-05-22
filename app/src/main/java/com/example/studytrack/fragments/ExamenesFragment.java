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
import com.example.studytrack.adapters.ExamenAdapter;
import com.example.studytrack.models.Examen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExamenesFragment extends Fragment {

    private RecyclerView examenesRecyclerView;
    private FloatingActionButton examenesFabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_examenes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        examenesRecyclerView = view.findViewById(R.id.examenesRecyclerView);
        examenesFabAdd = view.findViewById(R.id.examenesFabAdd);

        // Configuracion base del RecyclerView.
        examenesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        examenesRecyclerView.setHasFixedSize(true);

        // Datos de prueba hardcoded para validar el flujo visual y funcional.
        List<Examen> examenesDemo = new ArrayList<>();
        examenesDemo.add(new Examen(
            "Matematicas",
            "Funciones cuadraticas y factorizacion",
            "24/05/2026 - 10:00"
        ));
        examenesDemo.add(new Examen(
            "Historia",
            "Independencias latinoamericanas",
            "27/05/2026 - 08:30"
        ));
        examenesDemo.add(new Examen(
            "Programacion",
            "POO y colecciones en Java",
            "30/05/2026 - 14:00"
        ));

        ExamenAdapter examenAdapter = new ExamenAdapter(examenesDemo, examen -> {
            Bundle bundle = new Bundle();
            bundle.putString("materia", examen.getMateria());
            bundle.putString("tema", examen.getTema());
            bundle.putString("fechaHora", examen.getFechaHora());

            Navigation.findNavController(view).navigate(
                R.id.action_nav_examenes_to_detalleExamenFragment,
                bundle
            );
        });
        examenesRecyclerView.setAdapter(examenAdapter);

        examenesFabAdd.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                getString(R.string.toast_add_exam),
                Toast.LENGTH_SHORT
        ).show());
    }
}