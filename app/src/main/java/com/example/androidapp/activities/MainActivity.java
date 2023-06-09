package com.example.androidapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.AddNewTask;
import com.example.androidapp.OnDialogCloseListener;
import com.example.androidapp.R;
import com.example.androidapp.adapters.TaskAdapter;
import com.example.androidapp.databinding.ActivityMainBinding;
import com.example.androidapp.models.ToDo;
import com.example.androidapp.utilities.Constants;
import com.example.androidapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private StorageReference storageReference;
    private TaskAdapter taskAdapter;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isDarkModeOn()) {
            setTheme(R.style.Theme_AndroidApp_Dark);
        } else {
            setTheme(R.style.Theme_AndroidApp);
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageReference = FirebaseStorage.getInstance().getReference();

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
        getTodos();
    }

    private void setListeners() {
        binding.imageSingOut.setOnClickListener(v -> singOut());
        binding.fabNewChat.setOnClickListener(v ->
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));

        String imageString = preferenceManager.getString(Constants.KEY_IMAGE);
        if (imageString != null) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
        }
    }

    private void showToats(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToats("Nie mogę update'ować token'a"));
    }

    private void singOut() {
        showToats(getString(R.string.wylogowanie));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(update)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SingInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToats(getString(R.string.blad_wylogowanie)));
    }

    private void getTodos() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_TASKS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<ToDo> todoList = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            ToDo toDo = new ToDo();
                            toDo.task = queryDocumentSnapshot.getString(Constants.KEY_TASK_NAME);
                            toDo.due = queryDocumentSnapshot.getString(Constants.KEY_TASK_DUE);
                            toDo.status = queryDocumentSnapshot.getLong(Constants.KEY_TASK_STATUS).intValue();
                            String taskId = queryDocumentSnapshot.getString(Constants.KEY_TASK_ID);

                            // Load task image
                            String taskImageKey = queryDocumentSnapshot.getString(Constants.KEY_TASK_IMAGE);
                            if (taskImageKey != null) {
                                Uri imageUri = Uri.parse(taskImageKey);
                                Bitmap taskBitmap = getBitmapFromUri(imageUri);
                                toDo.taskImage = taskBitmap;
                            }

                            todoList.add(toDo);
                        }
                        if (todoList.size() > 0) {
                            taskAdapter = new TaskAdapter(todoList);
                            binding.tasksRecylerView.setAdapter(taskAdapter);
                            binding.tasksRecylerView.setVisibility(View.VISIBLE);
                        } else {
//                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }




    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", getString(R.string.brak_zadan)));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        getTodos();
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }

    private boolean isDarkModeOn() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
