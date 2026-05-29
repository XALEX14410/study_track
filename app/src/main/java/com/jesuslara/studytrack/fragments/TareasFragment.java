package com.jesuslara.studytrack.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.jesuslara.studytrack.R;
import com.jesuslara.studytrack.adapters.TareaAdapter;
import com.jesuslara.studytrack.databinding.DialogAddTareaBinding;
import com.jesuslara.studytrack.databinding.FragmentTareasBinding;
import com.jesuslara.studytrack.models.Tarea;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TareasFragment extends Fragment {

    private static final String TAG = "TAREAS";
    private static final String COLLECTION_USERS = "usuarios";
    private static final String COLLECTION_TAREAS = "tareas";
    private static final String FIELD_MATERIAS = "materias";
    private static final String FIELD_NOMBRE = "nombre";

    private static final String[] PRIORIDADES = {"Baja", "Media", "Alta"};
    private static final String[] RECORDATORIOS = {
            "Sin recordatorio",
            "10 minutos antes",
            "30 minutos antes",
            "1 hora antes",
            "1 dia antes"
    };

    private final List<Tarea> tareas = new ArrayList<>();
    private final List<String> nombresMaterias = new ArrayList<>();
    private final List<MateriaOption> materiasOptions = new ArrayList<>();

    private FragmentTareasBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TareaAdapter tareaAdapter;

    @Nullable
    private ListenerRegistration tareasListenerRegistration;
    @Nullable
    private ListenerRegistration materiasListenerRegistration;
    @Nullable
    private AlertDialog tareaDialog;
    @Nullable
    private ArrayAdapter<String> materiasDropdownAdapter;
    @Nullable
    private AutoCompleteTextView activeMateriaAutoComplete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTareasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        initMateriasDropdownAdapter();

        binding.fabAddTarea.setOnClickListener(v -> showTareaDialog(null));

        updateEmptyState();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachTareasRealtimeListener();
        attachMateriasRealtimeListener();
        cargarMaterias();
    }

    @Override
    public void onStop() {
        detachTareasRealtimeListener();
        detachMateriasRealtimeListener();
        super.onStop();
    }

    private void setupRecyclerView() {
        tareaAdapter = new TareaAdapter(new TareaAdapter.OnTareaActionListener() {
            @Override
            public void onTareaClick(@NonNull Tarea tarea) {
                openDetalleTarea(tarea);
            }

            @Override
            public void onEditarTarea(@NonNull Tarea tarea) {
                showTareaDialog(tarea);
            }

            @Override
            public void onEliminarTarea(@NonNull Tarea tarea) {
                confirmDeleteTarea(tarea);
            }

            @Override
            public void onCompletadaChanged(@NonNull Tarea tarea, boolean completada, int position) {
                updateCompletadaInFirestore(tarea, completada, position);
            }
        });

        binding.rvTareas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTareas.setHasFixedSize(true);
        binding.rvTareas.setAdapter(tareaAdapter);
    }

    private void initMateriasDropdownAdapter() {
        if (!isAdded()) {
            return;
        }

        if (materiasDropdownAdapter == null) {
            materiasDropdownAdapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.item_dropdown_materia,
                    android.R.id.text1,
                    nombresMaterias
            );
            Log.d(TAG, "initMateriasDropdownAdapter: adapter inicializado");
        }
    }

    private void attachTareasRealtimeListener() {
        if (tareasListenerRegistration != null) {
            return;
        }

        CollectionReference tareasCollection = getTareasCollection();
        if (tareasCollection == null) {
            renderTareas(Collections.emptyList());
            return;
        }

        tareasListenerRegistration = tareasCollection.addSnapshotListener(this::onTareasSnapshotChanged);
    }

    private void detachTareasRealtimeListener() {
        if (tareasListenerRegistration != null) {
            tareasListenerRegistration.remove();
            tareasListenerRegistration = null;
        }
    }

    private void attachMateriasRealtimeListener() {
        if (materiasListenerRegistration != null) {
            Log.d(TAG, "attachMateriasRealtimeListener: listener ya activo");
            return;
        }

        String uid = getCurrentUid();
        if (TextUtils.isEmpty(uid)) {
            Log.w(TAG, "attachMateriasRealtimeListener: uid nulo, no se consulta materias");
            clearMateriasData();
            return;
        }

        DocumentReference userDocument = getUserDocument(uid);
        Log.d(TAG, "attachMateriasRealtimeListener: escuchando " + buildPath(COLLECTION_USERS, uid));
        materiasListenerRegistration = userDocument.addSnapshotListener((documentSnapshot, error) ->
                onMateriasSnapshotChanged(uid, documentSnapshot, error));
    }

    private void detachMateriasRealtimeListener() {
        if (materiasListenerRegistration != null) {
            materiasListenerRegistration.remove();
            materiasListenerRegistration = null;
            Log.d(TAG, "detachMateriasRealtimeListener: listener removido");
        }
    }

    private void onMateriasSnapshotChanged(@NonNull String uid,
                                           @Nullable DocumentSnapshot documentSnapshot,
                                           @Nullable FirebaseFirestoreException error) {
        String userPath = buildPath(COLLECTION_USERS, uid);
        String fieldPath = userPath + "." + FIELD_MATERIAS;

        if (error != null) {
            logFirestoreError("onMateriasSnapshotChanged " + fieldPath, error);
            showSnackbar(getString(R.string.tareas_materias_error));
            return;
        }

        if (documentSnapshot == null || !documentSnapshot.exists()) {
            Log.w(TAG, "onMateriasSnapshotChanged: documento de usuario inexistente. uid=" + uid + ", path=" + userPath);
            clearMateriasData();
            return;
        }

        List<MateriaOption> parsed = parseMateriaOptionsFromArray(
                documentSnapshot.get(FIELD_MATERIAS),
                fieldPath
        );
        Log.d(TAG, "onMateriasSnapshotChanged: uid=" + uid + ", materias=" + parsed.size());
        applyMateriaOptions(parsed, "snapshot:" + fieldPath, true);
    }

    private void clearMateriasData() {
        materiasOptions.clear();
        nombresMaterias.clear();
        notifyMateriasDropdownChanged(false);
    }

    private void notifyMateriasDropdownChanged(boolean openIfPossible) {
        if (materiasDropdownAdapter == null) {
            return;
        }

        materiasDropdownAdapter.notifyDataSetChanged();
        Log.d(TAG, "notifyMateriasDropdownChanged: notifyDataSetChanged size=" + materiasDropdownAdapter.getCount());

        if (activeMateriaAutoComplete != null) {
            activeMateriaAutoComplete.setAdapter(null);
            activeMateriaAutoComplete.setAdapter(materiasDropdownAdapter);
            if (openIfPossible) {
                forceShowMateriasDropdown(activeMateriaAutoComplete, "snapshot_update");
            }
        }
    }

    private void onTareasSnapshotChanged(@Nullable QuerySnapshot querySnapshot,
                                         @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            Log.e(TAG, "Error escuchando tareas", error);
            showSnackbar(getString(R.string.tareas_listener_error));
            return;
        }

        List<Tarea> nuevasTareas = new ArrayList<>();
        if (querySnapshot != null) {
            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                Tarea tarea = parseTarea(documentSnapshot);
                if (tarea != null) {
                    nuevasTareas.add(tarea);
                }
            }
        }

        sortTareas(nuevasTareas);
        renderTareas(nuevasTareas);
    }

    @Nullable
    private Tarea parseTarea(@NonNull DocumentSnapshot documentSnapshot) {
        try {
            Tarea tarea = documentSnapshot.toObject(Tarea.class);
            if (tarea == null) {
                return null;
            }

            tarea.setId(documentSnapshot.getId());

            if (TextUtils.isEmpty(tarea.getNombre())) {
                String tituloLegacy = documentSnapshot.getString("titulo");
                if (!TextUtils.isEmpty(tituloLegacy)) {
                    tarea.setNombre(tituloLegacy);
                }
            }

            if (TextUtils.isEmpty(tarea.getPrioridad())) {
                tarea.setPrioridad(PRIORIDADES[0]);
            }

            if (TextUtils.isEmpty(tarea.getMateriaNombre())) {
                tarea.setMateriaNombre(getString(R.string.tarea_chip_materia_placeholder));
            }

            return tarea;
        } catch (Exception exception) {
            Log.e(TAG, "No se pudo parsear una tarea", exception);
            return null;
        }
    }

    private void sortTareas(@NonNull List<Tarea> source) {
        source.sort(new Comparator<Tarea>() {
            @Override
            public int compare(Tarea first, Tarea second) {
                if (first.isCompletada() != second.isCompletada()) {
                    return first.isCompletada() ? 1 : -1;
                }

                long firstTimestamp = first.getTimestampEntrega() != null
                        ? first.getTimestampEntrega()
                        : Long.MAX_VALUE;
                long secondTimestamp = second.getTimestampEntrega() != null
                        ? second.getTimestampEntrega()
                        : Long.MAX_VALUE;

                int byTimestamp = Long.compare(firstTimestamp, secondTimestamp);
                if (byTimestamp != 0) {
                    return byTimestamp;
                }

                long firstUpdated = first.getUpdatedAt() != null ? first.getUpdatedAt() : 0L;
                long secondUpdated = second.getUpdatedAt() != null ? second.getUpdatedAt() : 0L;
                return Long.compare(secondUpdated, firstUpdated);
            }
        });
    }

    private void renderTareas(@NonNull List<Tarea> nuevasTareas) {
        tareas.clear();
        tareas.addAll(nuevasTareas);

        if (binding == null || tareaAdapter == null) {
            return;
        }

        tareaAdapter.updateItems(nuevasTareas);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (binding == null) {
            return;
        }

        boolean isEmpty = tareas.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvTareas.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showTareaDialog(@Nullable Tarea tareaToEdit) {
        if (!isAdded() || binding == null) {
            return;
        }

        DialogAddTareaBinding dialogBinding = DialogAddTareaBinding.inflate(getLayoutInflater());
        setupDialogStaticFields(dialogBinding);
        setupDateAndTimePickers(dialogBinding);
        bindMateriasDropdown(dialogBinding.actvMateria);
        dialogBinding.tilMateria.setEndIconOnClickListener(v ->
            forceShowMateriasDropdown(dialogBinding.actvMateria, "end_icon"));
        prefillDialogData(dialogBinding, tareaToEdit);
        attachMateriasRealtimeListener();
        cargarMaterias();
        notifyMateriasDropdownChanged(false);

        int titleRes = tareaToEdit == null
                ? R.string.dialog_add_tarea_title
                : R.string.dialog_edit_tarea_title;
        int positiveRes = tareaToEdit == null
                ? R.string.dialog_action_guardar
                : R.string.dialog_action_actualizar;

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.dialog_action_cancelar, (d, which) -> d.dismiss())
                .setPositiveButton(positiveRes, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> saveDialogData(
                    tareaToEdit,
                    dialogBinding,
                    dialog,
                    positiveButton
            ));

            dialogBinding.actvMateria.post(() -> {
                dialogBinding.actvMateria.requestFocus();
                forceShowMateriasDropdown(dialogBinding.actvMateria, "dialog_on_show");
            });
        });

        dialog.setOnDismissListener(d -> {
            if (tareaDialog == dialog) {
                tareaDialog = null;
            }
            if (activeMateriaAutoComplete == dialogBinding.actvMateria) {
                activeMateriaAutoComplete = null;
            }
        });

        tareaDialog = dialog;
        dialog.show();
    }

    private void setupDialogStaticFields(@NonNull DialogAddTareaBinding dialogBinding) {
        ArrayAdapter<String> prioridadAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                PRIORIDADES
        );
        dialogBinding.actvPrioridad.setAdapter(prioridadAdapter);
        dialogBinding.actvPrioridad.setText(PRIORIDADES[0], false);

        ArrayAdapter<String> recordatorioAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                RECORDATORIOS
        );
        dialogBinding.actvRecordatorio.setAdapter(recordatorioAdapter);
        dialogBinding.actvRecordatorio.setText(RECORDATORIOS[0], false);
    }

    private void setupDateAndTimePickers(@NonNull DialogAddTareaBinding dialogBinding) {
        dialogBinding.etFecha.setOnClickListener(v -> showDatePicker(dialogBinding.etFecha));
        dialogBinding.tilFecha.setEndIconOnClickListener(v -> showDatePicker(dialogBinding.etFecha));

        dialogBinding.etHora.setOnClickListener(v -> showTimePicker(dialogBinding.etHora));
        dialogBinding.tilHora.setEndIconOnClickListener(v -> showTimePicker(dialogBinding.etHora));
    }

    private void showDatePicker(@NonNull TextView targetView) {
        if (!isAdded()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        String currentValue = readText(targetView);
        if (!TextUtils.isEmpty(currentValue)) {
            try {
                Date parsed = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentValue);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (ParseException ignored) {
                // Ignora y usa fecha actual
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> targetView.setText(String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(@NonNull TextView targetView) {
        if (!isAdded()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        String currentValue = readText(targetView);
        if (!TextUtils.isEmpty(currentValue)) {
            try {
                Date parsed = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(currentValue);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (ParseException ignored) {
                // Ignora y usa hora actual
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> targetView.setText(String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hourOfDay,
                        minute
                )),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void prefillDialogData(@NonNull DialogAddTareaBinding dialogBinding,
                                   @Nullable Tarea tareaToEdit) {
        if (tareaToEdit == null) {
            return;
        }

        dialogBinding.etNombre.setText(tareaToEdit.getNombre());
        dialogBinding.etDescripcion.setText(tareaToEdit.getDescripcion());
        dialogBinding.etFecha.setText(tareaToEdit.getFechaEntrega());
        dialogBinding.etHora.setText(tareaToEdit.getHoraEntrega());

        if (!TextUtils.isEmpty(tareaToEdit.getPrioridad())) {
            dialogBinding.actvPrioridad.setText(tareaToEdit.getPrioridad(), false);
        }
        if (!TextUtils.isEmpty(tareaToEdit.getAntelacion())) {
            dialogBinding.actvRecordatorio.setText(tareaToEdit.getAntelacion(), false);
        }

        String materiaNombre = resolveMateriaNombre(tareaToEdit);
        if (!TextUtils.isEmpty(materiaNombre)) {
            dialogBinding.actvMateria.setText(materiaNombre, false);
        }
    }

    private void bindMateriasDropdown(@NonNull AutoCompleteTextView autoCompleteTextView) {
        initMateriasDropdownAdapter();
        if (materiasDropdownAdapter == null) {
            Log.e(TAG, "bindMateriasDropdown: adapter de materias nulo");
            return;
        }

        activeMateriaAutoComplete = autoCompleteTextView;

        autoCompleteTextView.setAdapter(null);
        autoCompleteTextView.setAdapter(materiasDropdownAdapter);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        autoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        int verticalOffset = (int) (4 * requireContext().getResources().getDisplayMetrics().density);
        autoCompleteTextView.setDropDownVerticalOffset(verticalOffset);
        autoCompleteTextView.setText(autoCompleteTextView.getText(), false);

        autoCompleteTextView.setOnClickListener(v -> {
            forceShowMateriasDropdown(autoCompleteTextView, "on_click");
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "AutoComplete materias onFocusChange hasFocus=" + hasFocus);
            if (hasFocus) {
                forceShowMateriasDropdown(autoCompleteTextView, "on_focus");
            }
        });

        autoCompleteTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                forceShowMateriasDropdown(autoCompleteTextView, "on_touch");
            }
            return false;
        });

        autoCompleteTextView.setOnDismissListener(() ->
                Log.d(TAG, "AutoComplete materias dropdown cerrado"));

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = position >= 0 && position < nombresMaterias.size()
                    ? nombresMaterias.get(position)
                    : "";
            Log.d(TAG, "Materia seleccionada: position=" + position + ", nombre=" + selected);
        });

        materiasDropdownAdapter.notifyDataSetChanged();
        Log.d(TAG, "bindMateriasDropdown: notifyDataSetChanged count=" + materiasDropdownAdapter.getCount());
        forceShowMateriasDropdown(autoCompleteTextView, "bind_materias_dropdown");
    }

    private void cargarMaterias() {
        if (!isAdded()) {
            return;
        }

        String uid = getCurrentUid();
        if (TextUtils.isEmpty(uid)) {
            Log.w(TAG, "cargarMaterias: uid vacio, no se puede consultar Firestore");
            clearMateriasData();
            return;
        }

        String userPath = buildPath(COLLECTION_USERS, uid);
        String fieldPath = userPath + "." + FIELD_MATERIAS;
        Log.d(TAG, "cargarMaterias: leyendo " + fieldPath);

        getUserDocument(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        Log.w(TAG, "cargarMaterias: documento de usuario inexistente. path=" + userPath);
                        clearMateriasData();
                        return;
                    }

                    List<MateriaOption> parsed = parseMateriaOptionsFromArray(
                            documentSnapshot.get(FIELD_MATERIAS),
                            fieldPath
                    );
                    Log.d(TAG, "cargarMaterias: uid=" + uid + ", materias=" + parsed.size());
                    applyMateriaOptions(parsed, "cargarMaterias:" + fieldPath, true);
                })
                .addOnFailureListener(error -> {
                    logFirestoreError("cargarMaterias " + fieldPath, error);
                    showSnackbar(getString(R.string.tareas_materias_error));
                    clearMateriasData();
                });
    }

    private void applyMateriaOptions(@NonNull List<MateriaOption> options,
                                     @NonNull String sourceTag,
                                     boolean openIfPossible) {
        materiasOptions.clear();
        nombresMaterias.clear();

        for (MateriaOption option : options) {
            if (containsMateriaOption(option)) {
                Log.d(TAG,
                        "applyMateriaOptions: duplicada omitida source=" + sourceTag
                                + ", id=" + option.id
                                + ", nombre=" + option.nombre
                );
                continue;
            }
            materiasOptions.add(option);
            nombresMaterias.add(option.nombre);
        }

        materiasOptions.sort(Comparator.comparing(option -> option.nombre.toLowerCase(Locale.ROOT)));
        nombresMaterias.sort(String::compareToIgnoreCase);

        Log.d(TAG,
                "applyMateriaOptions: source=" + sourceTag
                        + ", total=" + nombresMaterias.size()
                        + ", listaFinal=" + nombresMaterias
        );

        notifyMateriasDropdownChanged(openIfPossible);
    }

    private boolean containsMateriaOption(@NonNull MateriaOption candidate) {
        for (MateriaOption existing : materiasOptions) {
            if (existing.id.equalsIgnoreCase(candidate.id)
                    || existing.nombre.equalsIgnoreCase(candidate.nombre)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private List<MateriaOption> parseMateriaOptionsFromArray(@Nullable Object materiasRaw,
                                                              @NonNull String sourcePath) {
        List<MateriaOption> options = new ArrayList<>();

        if (!(materiasRaw instanceof List<?>)) {
            Log.w(TAG, "parseMateriaOptionsFromArray: " + sourcePath
                    + " no es List (tipo=" + getTypeName(materiasRaw) + ")");
            return options;
        }

        List<?> list = (List<?>) materiasRaw;

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof Map<?, ?>)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) item;

            String idMateria = firstNonEmpty(
                    asString(map.get("idMateria")),
                    asString(map.get("id")),
                    "array-index-" + i
            );

            String nombre = firstNonEmpty(
                    asString(map.get(FIELD_NOMBRE)),
                    asString(map.get("materiaNombre")),
                    asString(map.get("name")),
                    asString(map.get("title")),
                    idMateria
            );

            if (TextUtils.isEmpty(nombre)) {
                continue;
            }

            String nombreLimpio = nombre.trim();
            if (TextUtils.isEmpty(nombreLimpio)) {
                continue;
            }

            options.add(new MateriaOption(idMateria, nombreLimpio));
        }

        return options;
    }

    private void logFirestoreError(@NonNull String origin,
                                   @NonNull Exception exception) {
        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;
            Log.e(TAG,
                    origin
                            + ": code=" + firestoreException.getCode()
                            + ", message=" + safeLogValue(firestoreException.getMessage()),
                    firestoreException
            );
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.e(TAG,
                        origin
                                + ": lectura bloqueada por Firebase Rules (PERMISSION_DENIED)."
                );
            }
            return;
        }

        Log.e(TAG,
                origin + ": " + safeLogValue(exception.getMessage()),
                exception
        );
    }

    @NonNull
    private String buildPath(@NonNull String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (TextUtils.isEmpty(segment)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(segment);
        }
        return builder.toString();
    }

    @NonNull
    private String safeLogValue(@Nullable String value) {
        return value == null ? "<null>" : value;
    }

    @NonNull
    private String getTypeName(@Nullable Object object) {
        return object == null ? "null" : object.getClass().getSimpleName();
    }

    @NonNull
    private String asString(@Nullable Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void forceShowMateriasDropdown(@NonNull AutoCompleteTextView autoCompleteTextView,
                                           @NonNull String origin) {
        if (!isAdded()) {
            Log.w(TAG, "forceShowMateriasDropdown(" + origin + "): fragment no agregado");
            return;
        }

        if (materiasDropdownAdapter == null) {
            Log.e(TAG, "forceShowMateriasDropdown(" + origin + "): adapter nulo");
            return;
        }

        int count = materiasDropdownAdapter.getCount();
        Log.d(TAG, "forceShowMateriasDropdown(" + origin + "): count=" + count);
        if (count == 0) {
            return;
        }

        activeMateriaAutoComplete = autoCompleteTextView;
        autoCompleteTextView.post(() -> {
            if (!autoCompleteTextView.isAttachedToWindow()) {
                Log.w(TAG, "forceShowMateriasDropdown(" + origin + "): view no adjunta a ventana");
                return;
            }

            autoCompleteTextView.requestFocus();
            autoCompleteTextView.setText(autoCompleteTextView.getText(), false);
            autoCompleteTextView.setSelection(autoCompleteTextView.length());
            autoCompleteTextView.dismissDropDown();
            autoCompleteTextView.showDropDown();
            Log.d(TAG, "forceShowMateriasDropdown(" + origin + "): showDropDown ejecutado");
        });
    }

    private void saveDialogData(@Nullable Tarea tareaToEdit,
                                @NonNull DialogAddTareaBinding dialogBinding,
                                @NonNull AlertDialog dialog,
                                @NonNull Button positiveButton) {
        clearDialogErrors(dialogBinding);

        TareaFormData formData = validateDialogData(dialogBinding);
        if (formData == null) {
            return;
        }

        CollectionReference tareasCollection = getTareasCollection();
        if (tareasCollection == null) {
            return;
        }

        positiveButton.setEnabled(false);

        DocumentReference documentReference;
        if (tareaToEdit != null && !TextUtils.isEmpty(tareaToEdit.getId())) {
            documentReference = tareasCollection.document(tareaToEdit.getId());
        } else {
            documentReference = tareasCollection.document();
        }

        long now = System.currentTimeMillis();

        Tarea payload = new Tarea();
        payload.setId(documentReference.getId());
        payload.setNombre(formData.nombre);
        payload.setDescripcion(formData.descripcion);
        payload.setFechaEntrega(formData.fecha);
        payload.setHoraEntrega(formData.hora);
        payload.setTimestampEntrega(formData.timestampEntrega);
        payload.setMateriaId(formData.materiaOption.id);
        payload.setMateriaNombre(formData.materiaOption.nombre);
        payload.setPrioridad(formData.prioridad);
        payload.setAntelacion(formData.recordatorio);
        payload.setCompletada(tareaToEdit != null && tareaToEdit.isCompletada());
        payload.setCreatedAt(tareaToEdit != null && tareaToEdit.getCreatedAt() != null
                ? tareaToEdit.getCreatedAt()
                : now);
        payload.setUpdatedAt(now);

        documentReference.set(payload)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) {
                        return;
                    }
                    dialog.dismiss();
                    int messageRes = tareaToEdit == null
                            ? R.string.tarea_creada_ok
                            : R.string.tarea_actualizada_ok;
                    showSnackbar(getString(messageRes));
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "No se pudo guardar la tarea", error);
                    positiveButton.setEnabled(true);
                    showSnackbar(getString(R.string.tarea_guardar_error));
                });
    }

    private void clearDialogErrors(@NonNull DialogAddTareaBinding dialogBinding) {
        dialogBinding.tilNombre.setError(null);
        dialogBinding.tilMateria.setError(null);
        dialogBinding.tilFecha.setError(null);
        dialogBinding.tilHora.setError(null);
        dialogBinding.tilPrioridad.setError(null);
    }

    @Nullable
    private TareaFormData validateDialogData(@NonNull DialogAddTareaBinding dialogBinding) {
        boolean hasError = false;

        String nombre = readText(dialogBinding.etNombre);
        String descripcion = readText(dialogBinding.etDescripcion);
        String materiaNombre = readText(dialogBinding.actvMateria);
        String fecha = readText(dialogBinding.etFecha);
        String hora = readText(dialogBinding.etHora);
        String prioridad = readText(dialogBinding.actvPrioridad);
        String recordatorio = readText(dialogBinding.actvRecordatorio);

        if (TextUtils.isEmpty(nombre)) {
            dialogBinding.tilNombre.setError(getString(R.string.tarea_validation_nombre));
            hasError = true;
        }

        MateriaOption materiaOption = findMateriaOptionByName(materiaNombre);
        if (TextUtils.isEmpty(materiaNombre) || materiaOption == null) {
            dialogBinding.tilMateria.setError(getString(R.string.tarea_validation_materia));
            hasError = true;
        }

        if (TextUtils.isEmpty(fecha)) {
            dialogBinding.tilFecha.setError(getString(R.string.tarea_validation_fecha));
            hasError = true;
        }

        if (TextUtils.isEmpty(hora)) {
            dialogBinding.tilHora.setError(getString(R.string.tarea_validation_hora));
            hasError = true;
        }

        if (TextUtils.isEmpty(prioridad)) {
            dialogBinding.tilPrioridad.setError(getString(R.string.tarea_validation_prioridad));
            hasError = true;
        }

        Long timestampEntrega = parseTimestampEntrega(fecha, hora);
        if (timestampEntrega == null) {
            dialogBinding.tilFecha.setError(getString(R.string.tarea_validation_fecha_hora_invalid));
            hasError = true;
        }

        if (hasError || materiaOption == null || timestampEntrega == null) {
            return null;
        }

        TareaFormData data = new TareaFormData();
        data.nombre = nombre;
        data.descripcion = descripcion;
        data.materiaOption = materiaOption;
        data.fecha = fecha;
        data.hora = hora;
        data.timestampEntrega = timestampEntrega;
        data.prioridad = prioridad;
        data.recordatorio = TextUtils.isEmpty(recordatorio) ? RECORDATORIOS[0] : recordatorio;
        return data;
    }

    @Nullable
    private Long parseTimestampEntrega(@NonNull String fecha, @NonNull String hora) {
        if (TextUtils.isEmpty(fecha) || TextUtils.isEmpty(hora)) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatter.setLenient(false);
            Date parsedDate = formatter.parse(fecha + " " + hora);
            return parsedDate != null ? parsedDate.getTime() : null;
        } catch (ParseException exception) {
            return null;
        }
    }

    private void updateCompletadaInFirestore(@NonNull Tarea tarea,
                                             boolean completada,
                                             int position) {
        boolean previousValue = !completada;

        if (TextUtils.isEmpty(tarea.getId())) {
            tarea.setCompletada(previousValue);
            if (tareaAdapter != null && position >= 0) {
                tareaAdapter.notifyItemChanged(position);
            }
            showSnackbar(getString(R.string.tarea_actualizar_estado_error));
            return;
        }

        CollectionReference tareasCollection = getTareasCollection();
        if (tareasCollection == null) {
            tarea.setCompletada(previousValue);
            if (tareaAdapter != null && position >= 0) {
                tareaAdapter.notifyItemChanged(position);
            }
            return;
        }

        tareasCollection.document(tarea.getId())
                .update(
                        "completada", completada,
                        "updatedAt", System.currentTimeMillis()
                )
                .addOnFailureListener(error -> {
                    Log.e(TAG, "No se pudo actualizar estado de completada", error);
                    if (tareaAdapter != null) {
                        tareaAdapter.restoreItemCompletionState(tarea.getId(), previousValue);
                    }
                    showSnackbar(getString(R.string.tarea_actualizar_estado_error));
                });
    }

    private void confirmDeleteTarea(@NonNull Tarea tarea) {
        if (!isAdded()) {
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.tarea_delete_confirm_title)
                .setMessage(R.string.tarea_delete_confirm_message)
                .setNegativeButton(R.string.dialog_action_cancelar, null)
                .setPositiveButton(R.string.tarea_menu_eliminar, (dialog, which) -> deleteTarea(tarea))
                .show();
    }

    private void deleteTarea(@NonNull Tarea tarea) {
        if (TextUtils.isEmpty(tarea.getId())) {
            showSnackbar(getString(R.string.tarea_delete_error));
            return;
        }

        CollectionReference tareasCollection = getTareasCollection();
        if (tareasCollection == null) {
            return;
        }

        Tarea backup = cloneTarea(tarea);
        DocumentReference documentReference = tareasCollection.document(tarea.getId());
        documentReference.delete()
                .addOnSuccessListener(unused -> {
                    if (binding == null || !isAdded()) {
                        return;
                    }

                    Snackbar.make(binding.getRoot(), R.string.tarea_eliminada_ok, Snackbar.LENGTH_LONG)
                            .setAction(R.string.tarea_action_undo, v -> restoreDeletedTarea(backup))
                            .show();
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "No se pudo eliminar la tarea", error);
                    showSnackbar(getString(R.string.tarea_delete_error));
                });
    }

    private void restoreDeletedTarea(@NonNull Tarea tarea) {
        CollectionReference tareasCollection = getTareasCollection();
        if (tareasCollection == null) {
            return;
        }

        tareasCollection.document(tarea.getId())
                .set(tarea)
                .addOnSuccessListener(unused -> showSnackbar(getString(R.string.tarea_restaurada_ok)))
                .addOnFailureListener(error -> {
                    Log.e(TAG, "No se pudo restaurar la tarea eliminada", error);
                    showSnackbar(getString(R.string.tarea_restore_error));
                });
    }

    private void openDetalleTarea(@NonNull Tarea tarea) {
        if (binding == null) {
            return;
        }

        try {
            String fallback = getString(R.string.detail_value_empty);
            Bundle bundle = new Bundle();
            bundle.putString("titulo", safeValue(tarea.getNombre(), fallback));
            bundle.putString("descripcion", safeValue(tarea.getDescripcion(), fallback));
            bundle.putString("fechaEntrega", safeValue(tarea.getFechaEntrega(), fallback));

            Navigation.findNavController(binding.getRoot()).navigate(
                    R.id.action_nav_tareas_to_detalleTareaFragment,
                    bundle
            );
        } catch (Exception exception) {
            Log.e(TAG, "No se pudo abrir detalle de tarea", exception);
            showSnackbar(getString(R.string.tareas_navigation_error));
        }
    }

    private String resolveMateriaNombre(@NonNull Tarea tarea) {
        if (!TextUtils.isEmpty(tarea.getMateriaNombre())) {
            return tarea.getMateriaNombre();
        }

        if (!TextUtils.isEmpty(tarea.getMateriaId())) {
            MateriaOption option = findMateriaOptionById(tarea.getMateriaId());
            if (option != null) {
                return option.nombre;
            }
        }

        return "";
    }

    @Nullable
    private MateriaOption findMateriaOptionByName(@Nullable String materiaNombre) {
        if (TextUtils.isEmpty(materiaNombre)) {
            return null;
        }

        String target = materiaNombre.trim();
        for (MateriaOption option : materiasOptions) {
            if (option.nombre.equalsIgnoreCase(target)) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    private MateriaOption findMateriaOptionById(@Nullable String materiaId) {
        if (TextUtils.isEmpty(materiaId)) {
            return null;
        }

        for (MateriaOption option : materiasOptions) {
            if (option.id.equals(materiaId)) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    private CollectionReference getTareasCollection() {
        String uid = getCurrentUid();
        if (TextUtils.isEmpty(uid)) {
            showSnackbar(getString(R.string.msg_session_expired));
            return null;
        }

        return firestore
                .collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_TAREAS);
    }

    @NonNull
    private DocumentReference getUserDocument(@NonNull String uid) {
        return firestore.collection(COLLECTION_USERS).document(uid);
    }

    @Nullable
    private String getCurrentUid() {
        if (auth == null) {
            Log.e(TAG, "getCurrentUid: FirebaseAuth no inicializado");
            return null;
        }

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "getCurrentUid: usuario no autenticado");
            return null;
        }

        String uid = firebaseUser.getUid();
        Log.d(TAG, "getCurrentUid: uid=" + uid);
        return uid;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    private String readText(@NonNull TextView view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }

    private String safeValue(@Nullable String value, @NonNull String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private void showSnackbar(@NonNull String message) {
        if (binding != null) {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @NonNull
    private Tarea cloneTarea(@NonNull Tarea source) {
        return new Tarea(
                source.getId(),
                source.getNombre(),
                source.getDescripcion(),
                source.getFechaEntrega(),
                source.getHoraEntrega(),
                source.getTimestampEntrega(),
                source.getMateriaId(),
                source.getMateriaNombre(),
                source.isCompletada(),
                source.getPrioridad(),
                source.getAntelacion(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    @Override
    public void onDestroyView() {
        detachTareasRealtimeListener();
        detachMateriasRealtimeListener();

        if (tareaDialog != null && tareaDialog.isShowing()) {
            tareaDialog.dismiss();
        }
        tareaDialog = null;
        activeMateriaAutoComplete = null;

        if (binding != null) {
            binding.rvTareas.setAdapter(null);
        }

        binding = null;
        super.onDestroyView();
    }

    private static final class MateriaOption {
        private final String id;
        private final String nombre;

        private MateriaOption(@NonNull String id, @NonNull String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    private static final class TareaFormData {
        private String nombre;
        private String descripcion;
        private MateriaOption materiaOption;
        private String fecha;
        private String hora;
        private long timestampEntrega;
        private String prioridad;
        private String recordatorio;
    }
}
