package com.example.studytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studytrack.R;

public class DetalleMateriaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_materia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView nombreValue = view.findViewById(R.id.detalleMateriaNombreValue);
        TextView descripcionValue = view.findViewById(R.id.detalleMateriaDescripcionValue);
        TextView horarioValue = view.findViewById(R.id.detalleMateriaHorarioValue);

        Bundle args = getArguments();
        String fallback = getString(R.string.detail_value_empty);

        if (args == null) {
            nombreValue.setText(fallback);
            descripcionValue.setText(fallback);
            horarioValue.setText(fallback);
            return;
        }

        // Recupera los datos enviados desde MateriasFragment via Bundle.
        nombreValue.setText(args.getString("nombre", fallback));
        descripcionValue.setText(args.getString("descripcion", fallback));
        horarioValue.setText(args.getString("horario", fallback));
    }
}
