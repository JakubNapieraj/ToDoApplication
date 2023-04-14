package com.example.androidapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidapp.databinding.AddNewTaskBinding;
import com.example.androidapp.utilities.Constants;
import com.example.androidapp.utilities.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private String dueDate = "";

    private AddNewTaskBinding binding;
    private PreferenceManager preferenceManager;
    private Context context;


    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddNewTaskBinding.inflate(inflater, container, false);
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

        binding.setDueTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();

                int DAY = calendar.get(Calendar.DATE);
                int MONTH = calendar.get(Calendar.MONTH);
                int YEAR = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        binding.setDueTv.setText(day + "/" + month + "/" + year);
                        dueDate = day + "/" + month + "/" + year;
                    }
                }, YEAR, MONTH, DAY);

                datePickerDialog.show();
            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    database.collection(Constants.KEY_COLLECTION_TASKS)
                            .add(taskMap)
                            .addOnSuccessListener(documentReference -> {
                                showToats("Task added");
                            })
                            .addOnFailureListener(exeption -> {
                                showToats(exeption.getMessage());
                            });
                }
                dismiss();
            }
        });

    }

    private void showToats(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
}
