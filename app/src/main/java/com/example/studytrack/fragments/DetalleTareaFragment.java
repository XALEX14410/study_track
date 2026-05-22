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

public class DetalleTareaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_tarea, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tituloValue = view.findViewById(R.id.detalleTareaTituloValue);
        TextView descripcionValue = view.findViewById(R.id.detalleTareaDescripcionValue);
        TextView fechaEntregaValue = view.findViewById(R.id.detalleTareaFechaEntregaValue);

        Bundle args = getArguments();
        String fallback = getString(R.string.detail_value_empty);

        if (args == null) {
            tituloValue.setText(fallback);
            descripcionValue.setText(fallback);
            fechaEntregaValue.setText(fallback);
            return;
        }

        // Recupera los datos enviados desde TareasFragment via Bundle.
        tituloValue.setText(args.getString("titulo", fallback));
        descripcionValue.setText(args.getString("descripcion", fallback));
        fechaEntregaValue.setText(args.getString("fechaEntrega", fallback));
    }
}
