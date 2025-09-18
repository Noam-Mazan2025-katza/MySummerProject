package com.example.mysummerproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity2 extends AppCompatActivity {

    private FirebaseAuth mAuth; // הצהרה על mAuth ברמת המחלקה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance(); // אתחול של mAuth

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null)
            updateUI(currentUser);
    }

    public void login(View view) {

    }
    public void register(View view){
        EditText emailEditText = findViewById(R.id.edittext_email);
        EditText passwordEditText =findViewById(R.id.edittext_password);
        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),passwordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity2.this, AddWorkoutActivity.class))
                        } else {
                            Toast.makeText(LoginActivity2.this,  "register faild:(", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
