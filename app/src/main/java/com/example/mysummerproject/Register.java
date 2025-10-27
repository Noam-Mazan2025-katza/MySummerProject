package com.example.mysummerproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class Register extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSelectImage, btnRegister;
    private ImageView imgProfile;
    private Uri imageUri;

    private FirebaseAuth refAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRegister = findViewById(R.id.btnRegister);
        imgProfile = findViewById(R.id.imgProfile);

        refAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnRegister.setOnClickListener(v -> createUser());
    }

    private void selectImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    public void createUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            pd.show();

            refAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();

                            if (task.isSuccessful()) {
                                FirebaseUser user = refAuth.getCurrentUser();

                                if (imageUri != null) {
                                    uploadImageAndSaveProfile(user, name);
                                } else {
                                    updateUserProfile(user, name, null);
                                }

                                Log.i("RegisterActivity", "User created successfully");
                                Toast.makeText(Register.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Exception exp = task.getException();
                                if (exp instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(Register.this, "Password too weak", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(Register.this, "User already exists", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(Register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseNetworkException) {
                                    Toast.makeText(Register.this, "Network error. Check connection.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Register.this, "Error: " + exp.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }

    private void uploadImageAndSaveProfile(FirebaseUser user, String name) {
        // 🔹 הצגת תיבת התקדמות בזמן העלאת תמונה
        pd.setMessage("Uploading image...");
        pd.show();

        StorageReference fileRef = storageRef.child("profile_pics/" + UUID.randomUUID().toString() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            pd.dismiss();
                            updateUserProfile(user, name, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(Register.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfile(FirebaseUser user, String name, String photoUrl) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);

        if (photoUrl != null) builder.setPhotoUri(Uri.parse(photoUrl));

        user.updateProfile(builder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        // 🔹 הצגת תמונת הפרופיל לאחר הרישום
                        if (user.getPhotoUrl() != null) {
                            imgProfile.setImageURI(user.getPhotoUrl());
                        }

                        // 🔹 מעבר אוטומטי למסך הלוגין
                        Intent intent = new Intent(Register.this, LoginActivity2.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
