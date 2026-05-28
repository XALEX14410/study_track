package com.jesuslara.studytrack.firebase;

import android.util.Log;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jesuslara.studytrack.models.UserModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Date;
import java.util.Map;

public class FirestoreUserRepository {

    private static final String TAG = "MATERIAS";

    private static final String COLLECTION_USERS = "usuarios";
    private static final String FIELD_UID = "uid";
    private static final String FIELD_NAME = "nombre";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHOTO = "foto";
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_REGISTER_DATE = "fechaRegistro";
    private static final String FIELD_LAST_LOGIN = "ultimoLogin";

    private final CollectionReference usersCollection;

    public interface UserRepositoryCallback {
        void onSuccess(UserModel userModel);

        void onError(Exception exception);
    }

    public FirestoreUserRepository() {
        usersCollection = FirebaseFirestore.getInstance().collection(COLLECTION_USERS);
    }

    public void syncUser(@NonNull FirebaseUser firebaseUser,
                         @NonNull String provider,
                         @NonNull UserRepositoryCallback callback) {

        String uid = firebaseUser.getUid();
        DocumentReference documentReference = usersCollection.document(uid);

        documentReference.get()
                .addOnSuccessListener(snapshot -> {
                    long now = System.currentTimeMillis();
                    long registerDate = snapshot.exists()
                            ? getLong(snapshot, FIELD_REGISTER_DATE, now)
                            : now;

                    String displayName = firstNonEmpty(
                            snapshot.getString(FIELD_NAME),
                            firebaseUser.getDisplayName(),
                            ""
                    );
                    String photoUrl = firstNonEmpty(
                            snapshot.getString(FIELD_PHOTO),
                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                            ""
                    );

                    UserModel userModel = new UserModel(
                            uid,
                            displayName,
                            firebaseUser.getEmail(),
                            photoUrl,
                            provider,
                            registerDate,
                            now
                    );

                    Map<String, Object> data = toFirestoreMap(userModel);
                    data.put(FIELD_LAST_LOGIN, FieldValue.serverTimestamp());

                    documentReference.set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess(userModel))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getUserById(@NonNull String uid, @NonNull UserRepositoryCallback callback) {
        usersCollection.document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError(new IllegalStateException("User not found in Firestore"));
                        return;
                    }
                    callback.onSuccess(fromSnapshot(snapshot));
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateUserProfile(@NonNull String uid,
                                  @NonNull String displayName,
                                  String photoUrl,
                                  @NonNull UserRepositoryCallback callback) {

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_NAME, displayName.trim());
        updates.put(FIELD_PHOTO, firstNonEmpty(photoUrl, ""));
        updates.put(FIELD_LAST_LOGIN, FieldValue.serverTimestamp());

        usersCollection.document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> getUserById(uid, callback))
                .addOnFailureListener(callback::onError);
    }

    private UserModel fromSnapshot(DocumentSnapshot snapshot) {
        // Usamos toObject para que Firestore deserialice automaticamente el array
        // anidado "materias" (con sus examenes y tareas internas). Si el documento
        // estuviese vacio o corrupto, devolvemos un UserModel con los campos basicos.
        UserModel userModel;
        try {
            userModel = snapshot.toObject(UserModel.class);
        } catch (Exception exception) {
            Log.e(TAG, "Error deserializando usuario desde Firestore", exception);
            userModel = new UserModel();
        }
        if (userModel == null) {
            userModel = new UserModel();
        }
        if (TextUtils.isEmpty(userModel.getUid())) {
            userModel.setUid(firstNonEmpty(snapshot.getString(FIELD_UID), snapshot.getId()));
        }
        if (userModel.getNombre() == null) {
            userModel.setNombre("");
        }
        if (userModel.getEmail() == null) {
            userModel.setEmail("");
        }
        if (userModel.getFoto() == null) {
            userModel.setFoto("");
        }
        if (userModel.getProvider() == null) {
            userModel.setProvider("");
        }
        if (userModel.getFechaRegistro() == 0L) {
            userModel.setFechaRegistro(getLong(snapshot, FIELD_REGISTER_DATE, 0L));
        }
        if (userModel.getUltimoLogin() == null) {
            userModel.setUltimoLogin(getTimestamp(snapshot, FIELD_LAST_LOGIN, null));
        }
        return userModel;
    }

    private Map<String, Object> toFirestoreMap(UserModel userModel) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_UID, userModel.getUid());
        data.put(FIELD_NAME, userModel.getNombre());
        data.put(FIELD_EMAIL, userModel.getEmail());
        data.put(FIELD_PHOTO, userModel.getFoto());
        data.put(FIELD_PROVIDER, userModel.getProvider());
        data.put(FIELD_REGISTER_DATE, userModel.getFechaRegistro());
        data.put(FIELD_LAST_LOGIN, userModel.getUltimoLogin());
        return data;
    }

    private long getLong(DocumentSnapshot snapshot, String key, long defaultValue) {
        Object value = snapshot.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }
        return defaultValue;
    }

    private Timestamp getTimestamp(DocumentSnapshot snapshot, String key, Timestamp defaultValue) {
        Object value = snapshot.get(key);
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            return millis > 0L ? new Timestamp(new Date(millis)) : defaultValue;
        }
        return defaultValue;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return "";
    }
}
