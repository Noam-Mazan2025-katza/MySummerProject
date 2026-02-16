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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Register extends BaseActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSelectImage, btnRegister;
    private ImageView imgProfile;
    private Uri imageUri;

    private FirebaseAuth refAuth;
    private ProgressDialog pd;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // טעינת העיצוב
        setContentLayout(R.layout.activity_register);
        setupMenu();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חיבור רכיבים
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRegister = findViewById(R.id.btnRegister);
        imgProfile = findViewById(R.id.imgProfile);

        // פיירבייס
        refAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // דיאלוג המתנה
        pd = new ProgressDialog(this);
        pd.setTitle("מתחבר...");
        pd.setMessage("יוצר משתמש, רק רגע");

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
            imgProfile.setImageURI(imageUri);
        }
    }

    public void createUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "חסרים פרטים!", Toast.LENGTH_SHORT).show();
            return;
        }

        pd.show();

        // 1. יצירת משתמש ב-Auth (אימייל וסיסמה)
        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = refAuth.getCurrentUser();
                        // 2. שמירת הנתונים ב-Firestore
                        saveUserToFirestore(user, name);
                    } else {
                        pd.dismiss();
                        // טיפול בשגיאות
                        Exception exp = task.getException();
                        if (exp instanceof FirebaseAuthWeakPasswordException)
                            Toast.makeText(this, "סיסמה חלשה מדי", Toast.LENGTH_SHORT).show();
                        else if (exp instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(this, "המייל הזה כבר קיים", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(this, "שגיאה: " + exp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name) {
        String base64Image = "";

        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                base64Image = encodeImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", user.getEmail());

        // --- שינוי 1: שיניתי את המפתח ל-imageData כדי שיתאים ל-MainActivity ---
        userData.put("imageData", base64Image);

        userData.put("points", 0);
        userData.put("totalMinutes", 0);
        userData.put("workoutCount", 0);

        // --- שינוי 2: אנחנו שומרים בתיקיית "users" (וודא שגם ב-Main המאזין הוא ל-"users") ---
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    updateUserProfile(user, name);
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "שגיאה בשמירת נתונים", Toast.LENGTH_SHORT).show();
                });
    }

    // המרת תמונה לטקסט (Base64)
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // דחיסה כדי לא לתפוס המון מקום
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void updateUserProfile(FirebaseUser user, String name) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);

        if (imageUri != null) {
            builder.setPhotoUri(imageUri);
        }

        user.updateProfile(builder.build())
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    Toast.makeText(this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, MainActivity.class));
                    finish();
                });
    }
}