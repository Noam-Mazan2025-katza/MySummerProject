package com.example.mysummerproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Register extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSelectImage, btnRegister;
    private ImageView imgProfile;
    private Uri imageUri;

    private FirebaseAuth refAuth;
    private ProgressDialog pd;

    private FirebaseFirestore db;
    private CollectionReference refImages;

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
        db = FirebaseFirestore.getInstance();
        refImages = db.collection("images");

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
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d("Register", "Selected image URI: " + imageUri.toString());
            imgProfile.setImageURI(imageUri);
        }
    }

    public void createUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = refAuth.getCurrentUser();
                        if (user != null) {
                            if (imageUri != null) {
                                saveImageAndProfile(user, name);
                            } else {
                                updateUserProfile(user, name, null);
                            }
                            Toast.makeText(Register.this, "User created successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception exp = task.getException();
                        if (exp instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "Password too weak", Toast.LENGTH_SHORT).show();
                        } else if (exp instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                        } else if (exp instanceof FirebaseNetworkException) {
                            Toast.makeText(this, "Network error. Check connection.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + exp.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveImageAndProfile(FirebaseUser user, String name) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            byte[] compressed = compressImage(bitmap);

            String base64Image = Base64.encodeToString(compressed, Base64.DEFAULT);

            refImages.document(user.getUid())
                    .set(new UserImage(name, user.getEmail(), base64Image))
                    .addOnSuccessListener(aVoid -> {
                        updateUserProfile(user, name, imageUri.toString()); // ✅ עדכון הפרופיל כולל שמירה בלוקל
                        Toast.makeText(this, "Image uploaded to Firestore!", Toast.LENGTH_SHORT).show();
                    })

                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateUserProfile(user, name, imageUri.toString());
                    });


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateUserProfile(user, name, null);
        }
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        int maxSize = 1_048_576; // 1MB
        int quality = 100;
        while (imageBytes.length > maxSize && quality > 5) {
            baos.reset();
            quality -= 5;
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            imageBytes = baos.toByteArray();
        }
        return imageBytes;
    }

    private void updateUserProfile(FirebaseUser user, String name, String photoUrl) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            builder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(builder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.reload();
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        // שמירה בלוקל עם הכתובת הנכונה של התמונה
                        PrefsRepo.addUser(this, name, photoUrl != null ? Uri.parse(photoUrl) : null);
                        PrefsRepo.addPoints(this, name, 0);
                        PrefsRepo.setBadge(this, name, false);


                        startActivity(new Intent(Register.this, MainActivity.class));
                        finish();
                    }
                });
    }


    // 🔹 מחלקה פנימית עבור שמירת נתוני משתמש + תמונה ב-Firestore
    private static class UserImage {
        public String name;
        public String email;
        public String imageData; // Base64
        public UserImage() {}
        public UserImage(String name, String email, String imageData) {
            this.name = name;
            this.email = email;
            this.imageData = imageData;
        }
    }
}
