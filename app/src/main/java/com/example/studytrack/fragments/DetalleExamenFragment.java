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

public class DetalleExamenFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_examen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView materiaValue = view.findViewById(R.id.detalleExamenMateriaValue);
        TextView temaValue = view.findViewById(R.id.detalleExamenTemaValue);
        TextView fechaHoraValue = view.findViewById(R.id.detalleExamenFechaHoraValue);

        Bundle args = getArguments();
        String fallback = getString(R.string.detail_value_empty);

        if (args == null) {
            materiaValue.setText(fallback);
            temaValue.setText(fallback);
            fechaHoraValue.setText(fallback);
            return;
        }

        // Recupera los datos enviados desde ExamenesFragment via Bundle.
        materiaValue.setText(args.getString("materia", fallback));
        temaValue.setText(args.getString("tema", fallback));
        fechaHoraValue.setText(args.getString("fechaHora", fallback));
    }
}
