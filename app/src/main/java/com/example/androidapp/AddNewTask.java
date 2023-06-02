package com.example.androidapp;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidapp.databinding.AddNewTaskBinding;
import com.example.androidapp.utilities.Constants;
import com.example.androidapp.utilities.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private String dueDate = "";

    private AddNewTaskBinding binding;
    private PreferenceManager preferenceManager;
    private Context context;
    private ImageView taskImage;
    private String taskImageString;


    private StorageReference storageReference;
    private String imagePath = "images";
    private TextView photoButton;
    private UUID taskId;
    private ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result != null && result.getResultCode() == RESULT_OK) {
                    Intent gallery = result.getData();
                    if(gallery != null) {
                        uploadImage(gallery);
                    }
                }
            }
    );


    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AddNewTaskBinding.inflate(inflater, container, false);

        taskId = UUID.randomUUID();

        storageReference = FirebaseStorage.getInstance().getReference();
        photoButton = binding.getRoot().findViewById(R.id.photo_button);
        taskImage = binding.getRoot().findViewById(R.id.task_image);
        photoButton.setOnClickListener((unused) -> {
            Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForResult.launch(openGallery);
        });

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(context);

        binding.taskEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals("")) {
                    binding.save.setEnabled(false);
                    binding.save.setBackgroundColor(Color.GRAY);
                } else {
                    binding.save.setEnabled(true);
                    binding.save.setBackgroundColor(getResources().getColor(R.color.icon_background));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.setDueTv.setOnClickListener(v -> setDue());

        binding.save.setOnClickListener(v -> {saveTask(); dismiss();});

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }

    private void saveTask() {
        String task = binding.taskEditText.getText().toString();
        if (task.isEmpty()) {
            showToats("Empty task not Allowed");
        } else {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put(Constants.KEY_TASK_NAME, task);
            taskMap.put(Constants.KEY_TASK_DUE, dueDate);
            taskMap.put(Constants.KEY_TASK_STATUS, 0);
            taskMap.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            taskMap.put(Constants.KEY_TASK_ID, taskId.toString());
            taskMap.put(Constants.KEY_TASK_IMAGE, taskImageString);
            database.collection(Constants.KEY_COLLECTION_TASKS)
                    .add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        showToats("Task added");
                    })
                    .addOnFailureListener(exception -> showToats(exception.getMessage()));
        }
    }
    private void uploadImage(Intent gallery) {
        Uri imageUri = gallery.getData();

        if (imageUri != null) {
            taskImageString = imageUri.toString();
            preferenceManager.putString(Constants.KEY_TASK_IMAGE, imageUri.toString());
            StorageReference fileRef = storageReference.child(imagePath).child(taskId.toString() + ".jpg");
            Picasso.get().load(imageUri).into(taskImage);
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {

                    })
                    .addOnFailureListener(exception -> {

                    });
        }
    }


    private void setDue() {
        Calendar calendar = Calendar.getInstance();

        int DAY = calendar.get(Calendar.DATE);
        int MONTH = calendar.get(Calendar.MONTH);
        int YEAR = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(context,
                        (datePicker, year, month, day) -> {
                            month = month + 1;
                            binding.setDueTv.setText(day + "/" + month + "/" + year);
                            dueDate = day + "/" + month + "/" + year;
                        },
                        YEAR, MONTH, DAY);

        datePickerDialog.show();
    }

    private void showToats(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}