package com.example.mysummerproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class OpeningScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_screen);

        Button startButton = findViewById(R.id.startButton);

        // כאשר לוחצים על הכפתור – מעבר לדף הראשי
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(OpeningScreen.this, MainActivity.class);
            startActivity(intent);
            finish(); // כדי שמסך הפתיחה לא יחזור כשעושים Back
        });
    }
}