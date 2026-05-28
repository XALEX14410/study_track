package com.jesuslara.studytrack.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.adapters.MateriaAdapter;
import com.jesuslara.studytrack.models.Examen;
import com.jesuslara.studytrack.models.Materia;
import com.jesuslara.studytrack.models.Tarea;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MateriasFragment extends Fragment {

    private static final String TAG = "MATERIAS";
    private static final String COLLECTION_USERS = "usuarios";

    private RecyclerView materiasRecyclerView;
    private FloatingActionButton materiasFabAdd;
    private ProgressBar materiasProgressBar;
    private TextView materiasEmptyTextView;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService materiasExecutor = Executors.newSingleThreadExecutor();
    private MateriaAdapter adapter;

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

        try {
            materiasRecyclerView = view.findViewById(R.id.materiasRecyclerView);
            materiasFabAdd = view.findViewById(R.id.materiasFabAdd);
            materiasProgressBar = view.findViewById(R.id.materiasProgressBar);
            materiasEmptyTextView = view.findViewById(R.id.materiasEmptyTextView);

            validateViewReferences();

            adapter = new MateriaAdapter(new ArrayList<>(), this::openDetalleMateria);

            materiasRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            materiasRecyclerView.setHasFixedSize(true);
            materiasRecyclerView.setAdapter(adapter);

            materiasFabAdd.setOnClickListener(v -> Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_add_subject),
                    Toast.LENGTH_SHORT
            ).show());

            loadMaterias();
        } catch (Exception exception) {
            Log.e(TAG, "Error inicializando pantalla de materias", exception);
            showEmptyState(getString(R.string.materias_error));
        }
    }

    /**
     * Lee el documento usuarios/{uid} y deserializa el campo array "materias"
     * directamente desde el UserModel. La estructura es:
     *   usuarios/{uid}
     *       materias: [ { idMateria, nombre, aula, profesor, estado,
     *                     examenes: [...], tareas: [...] }, ... ]
     */
    private void loadMaterias() {
        try {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Log.e(TAG, "No hay usuario autenticado al cargar materias");
                showEmptyState(getString(R.string.materias_empty));
                return;
            }

            showLoading();
            String genericError = getString(R.string.materias_error);

            FirebaseFirestore.getInstance()
                    .collection(COLLECTION_USERS)
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(materiasExecutor, doc -> {
                        try {
                            List<Materia> result = new ArrayList<>();
                            if (doc != null && doc.exists()) {
                                result.addAll(parseMateriasSnapshot(doc));
                                result = sanitizeMaterias(result);
                            }
                            postRender(result);
                        } catch (Exception exception) {
                            Log.e("MATERIAS", "Error cargando materias", exception);
                            postError(genericError, exception);
                        }
                    })
                    .addOnFailureListener(materiasExecutor, error -> {
                        Log.e("MATERIAS", "Error cargando materias", error);
                        postError(genericError, error);
                    });
        } catch (Exception exception) {
            Log.e(TAG, "Error iniciando carga de materias", exception);
            showEmptyState(getString(R.string.materias_error));
        }
    }

    private void renderMaterias(List<Materia> nuevas) {
        try {
            if (adapter == null) {
                Log.e(TAG, "Adapter de materias no inicializado");
                showEmptyState(getString(R.string.materias_error));
                return;
            }

            List<Materia> safeList = nuevas != null ? nuevas : new ArrayList<>();
            adapter.updateItems(safeList);

            materiasProgressBar.setVisibility(View.GONE);
            if (safeList.isEmpty()) {
                materiasRecyclerView.setVisibility(View.GONE);
                materiasEmptyTextView.setVisibility(View.VISIBLE);
                materiasEmptyTextView.setText(R.string.materias_empty);
            } else {
                materiasRecyclerView.setVisibility(View.VISIBLE);
                materiasEmptyTextView.setVisibility(View.GONE);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Error renderizando materias", exception);
            showEmptyState(getString(R.string.materias_error));
        }
    }

    private void showLoading() {
        if (!isUiReady()) {
            return;
        }
        materiasProgressBar.setVisibility(View.VISIBLE);
        materiasEmptyTextView.setVisibility(View.GONE);
        materiasRecyclerView.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        if (!isUiReady()) {
            return;
        }
        materiasProgressBar.setVisibility(View.GONE);
        materiasRecyclerView.setVisibility(View.GONE);
        materiasEmptyTextView.setVisibility(View.VISIBLE);
        materiasEmptyTextView.setText(message);
    }

    private void openDetalleMateria(Materia materia) {
        if (materia == null || !isUiReady()) {
            return;
        }

        try {
            String fallback = getString(R.string.detail_value_empty);
            Bundle args = new Bundle();
            args.putString("nombre", safeValue(materia.getNombre(), fallback));
            args.putString("descripcion", getString(
                    R.string.materia_profesor_format,
                    safeValue(materia.getProfesor(), fallback)));
            args.putString("horario", getString(
                    R.string.materia_aula_format,
                    safeValue(materia.getAula(), fallback)));

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_nav_materias_to_detalleMateriaFragment, args);
        } catch (Exception exception) {
            Log.e(TAG, "Error abriendo detalle de materia", exception);
            Toast.makeText(requireContext(),
                    getString(R.string.materias_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private List<Materia> sanitizeMaterias(List<Materia> source) {
        List<Materia> sanitized = new ArrayList<>();
        for (Materia materia : source) {
            if (materia == null) {
                continue;
            }

            if (materia.getExamenes() == null) {
                materia.setExamenes(new ArrayList<>());
            }
            if (materia.getTareas() == null) {
                materia.setTareas(new ArrayList<>());
            }
            if (TextUtils.isEmpty(materia.getEstado())) {
                materia.setEstado(Materia.ESTADO_INACTIVA);
            }
            if (materia.getNombre() == null) {
                materia.setNombre("");
            }
            if (materia.getProfesor() == null) {
                materia.setProfesor("");
            }
            if (materia.getAula() == null) {
                materia.setAula("");
            }

            sanitized.add(materia);
        }
        return sanitized;
    }

    private List<Materia> parseMateriasSnapshot(DocumentSnapshot doc) {
        List<Materia> materiasDeserializadas = new ArrayList<>();
        Object materiasRaw = doc.get("materias");

        if (!(materiasRaw instanceof List<?>)) {
            return materiasDeserializadas;
        }

        for (Object materiaRaw : (List<?>) materiasRaw) {
            if (!(materiaRaw instanceof Map<?, ?>)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> materiaMap = (Map<String, Object>) materiaRaw;

            Materia materia = new Materia();
            materia.setIdMateria(asString(materiaMap.get("idMateria")));
            materia.setNombre(asString(materiaMap.get("nombre")));
            materia.setAula(asString(materiaMap.get("aula")));
            materia.setProfesor(asString(materiaMap.get("profesor")));
            String estado = asString(materiaMap.get("estado"));
            materia.setEstado(TextUtils.isEmpty(estado) ? Materia.ESTADO_INACTIVA : estado);
            materia.setExamenes(parseExamenes(materiaMap.get("examenes")));
            materia.setTareas(parseTareas(materiaMap.get("tareas")));

            materiasDeserializadas.add(materia);
        }

        return materiasDeserializadas;
    }

    private List<Examen> parseExamenes(Object rawExamenes) {
        List<Examen> examenes = new ArrayList<>();
        if (!(rawExamenes instanceof List<?>)) {
            return examenes;
        }

        for (Object examenRaw : (List<?>) rawExamenes) {
            if (!(examenRaw instanceof Map<?, ?>)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> examenMap = (Map<String, Object>) examenRaw;

            Examen examen = new Examen();
            examen.setMateria(asString(examenMap.get("materia")));
            examen.setTema(asString(examenMap.get("tema")));
            examen.setFechaHora(asString(examenMap.get("fechaHora")));
            examenes.add(examen);
        }

        return examenes;
    }

    private List<Tarea> parseTareas(Object rawTareas) {
        List<Tarea> tareas = new ArrayList<>();
        if (!(rawTareas instanceof List<?>)) {
            return tareas;
        }

        for (Object tareaRaw : (List<?>) rawTareas) {
            if (!(tareaRaw instanceof Map<?, ?>)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> tareaMap = (Map<String, Object>) tareaRaw;

            Tarea tarea = new Tarea();
            tarea.setTitulo(asString(tareaMap.get("titulo")));
            tarea.setDescripcion(asString(tareaMap.get("descripcion")));
            tarea.setFechaEntrega(asString(tareaMap.get("fechaEntrega")));
            tareas.add(tarea);
        }

        return tareas;
    }

    private void postRender(List<Materia> result) {
        mainHandler.post(() -> {
            if (!isUiReady()) {
                return;
            }
            renderMaterias(result);
        });
    }

    private void postError(String message, @Nullable Exception exception) {
        mainHandler.post(() -> {
            if (!isUiReady()) {
                return;
            }
            showEmptyState(message);
            String detail = exception != null && !TextUtils.isEmpty(exception.getLocalizedMessage())
                    ? exception.getLocalizedMessage()
                    : message;
            Toast.makeText(requireContext(), detail, Toast.LENGTH_SHORT).show();
        });
    }

    private void validateViewReferences() {
        if (materiasRecyclerView == null
                || materiasFabAdd == null
                || materiasProgressBar == null
                || materiasEmptyTextView == null) {
            throw new IllegalStateException("Views de Materias no inicializadas correctamente");
        }
    }

    private boolean isUiReady() {
        return isAdded()
                && getView() != null
                && materiasRecyclerView != null
                && materiasProgressBar != null
                && materiasEmptyTextView != null;
    }

    private String safeValue(@Nullable String value, String fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        return value;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        materiasRecyclerView = null;
        materiasFabAdd = null;
        materiasProgressBar = null;
        materiasEmptyTextView = null;
        adapter = null;
    }

    @Override
    public void onDestroy() {
        materiasExecutor.shutdownNow();
        super.onDestroy();
    }
}