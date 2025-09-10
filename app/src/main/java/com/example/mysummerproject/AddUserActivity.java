package com.example.mysummerproject;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddUserActivity extends AppCompatActivity {
    private EditText etName;
    private ImageView ivAvatar;
    private Uri pickedAvatar;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedAvatar = uri;
                    ivAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        etName = findViewById(R.id.etName);
        ivAvatar = findViewById(R.id.ivAvatar);
        Button btnPick = findViewById(R.id.btnPickAvatar);
        Button btnSave = findViewById(R.id.btnSaveUser);
        Button returnBtn= findViewById(R.id.buttonReturn);

        btnPick.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("שם חובה");
                return;
            }
            PrefsRepo.addUser(this, name, pickedAvatar);
            Toast.makeText(this, "נוסף משתמש: " + name, Toast.LENGTH_SHORT).show();
            finish(); // חוזר ל-MainActivity; onResume שם ירענן את הרשימה
        });
       btnSave.setOnClickListener(v ->{
           startActivity(new Intent(AddUserActivity.this, MainActivity.class));
    });
    }
}