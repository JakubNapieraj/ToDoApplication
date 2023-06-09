package com.example.androidapp.activities;

import static com.example.androidapp.AddNewTask.TAG;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.databinding.ActivitySingInBinding;
import com.example.androidapp.utilities.Constants;
import com.example.androidapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SingInActivity extends AppCompatActivity {

    private ActivitySingInBinding binding;
    private PreferenceManager preferenceManager;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseLogin();

        if (isDarkModeOn()) {
            setTheme(R.style.Theme_AndroidApp_Dark);
        } else {
            setTheme(R.style.Theme_AndroidApp);
        }
        setContentView(R.layout.activity_sing_in);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySingInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount
                .setOnClickListener(c ->
                        startActivity(new Intent(getApplicationContext(), SingUpActivity.class)));
        binding.buttonSingIn.setOnClickListener(c -> {
            if (isValidSingInDetails()){
                signIn();
            }
        });
    }

    private void signIn(){
        lodading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() >0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                         preferenceManager.putBoolen(Constants.KEY_IS_SIGNED_IN, true);
                         preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                         preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                         preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                         Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        lodading(false);
                        showToats(getString(R.string.blad_logowania));
                    }
                });
    }

    private void showToats(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSingInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToats(getString(R.string.wprowadz_email));
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToats(getString(R.string.wprowadz_email));
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToats(getString(R.string.wprowadz_haslo));
            return false;
        }
        return true;
    }

    private void lodading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSingIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSingIn.setVisibility(View.VISIBLE);
        }
    }

private void firebaseLogin() {
    firebaseAuth.signInAnonymously()
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // User is signed in anonymously
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        // Perform actions for authenticated user
                    } else {
                        // An error occurred while signing in anonymously
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        // Handle the error
                    }
                }
            });
}
    private boolean isDarkModeOn() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}