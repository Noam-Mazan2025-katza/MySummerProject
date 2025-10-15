package com.example.mysummerproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity2 extends AppCompatActivity {

    private static final String TAG = "LoginActivity2";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        // Initialize Firebase Auth (כמו בדוקו)
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // כמו בדוקו: בדיקה האם יש משתמש מחובר ועדכון UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // מחובר לכפתור "Login" דרך android:onClick="login" ב-XML
    public void login(View view) {
        // כאן מכניסים את ה-Custom Token שהשרת שלך יוצר בעזרת Firebase Admin SDK
        String mCustomToken = "REPLACE_WITH_CUSTOM_TOKEN_FROM_YOUR_SERVER";

        mAuth.signInWithCustomToken(mCustomToken)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity2.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // לא בשימוש ב-Custom Auth (השרת אחראי לאימות) — משאיר ריק כדי לא לשבור onClick ב-XML אם קיים
    public void register(View view) { }

    // בדיוק כמו בדוקו — אפשר להשאיר ריק או לממש בהמשך מעבר למסך הבא
    private void updateUI(FirebaseUser user) { }
}
