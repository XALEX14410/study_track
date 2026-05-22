package com.example.studytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studytrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MateriasFragment extends Fragment {

    private RecyclerView materiasRecyclerView;
    private FloatingActionButton materiasFabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_materias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        materiasRecyclerView = view.findViewById(R.id.materiasRecyclerView);
        materiasFabAdd = view.findViewById(R.id.materiasFabAdd);

        // Configuracion base del listado; el adapter se conecta en la siguiente iteracion.
        materiasRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        materiasRecyclerView.setHasFixedSize(true);

        materiasFabAdd.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                getString(R.string.toast_add_subject),
                Toast.LENGTH_SHORT
        ).show());
    }
}