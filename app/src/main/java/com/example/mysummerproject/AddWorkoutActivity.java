package com.example.mysummerproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue; // חשוב!
import com.google.firebase.firestore.FirebaseFirestore; // חשוב!

public class AddWorkoutActivity extends BaseActivity {

    private Spinner spType;
    private SeekBar sbMinutes;
    private TextView tvMinutes;

    private FirebaseUser fbUser;
    private FirebaseFirestore db; // הוספנו משתנה ל-DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.activity_add_workout);
        setupMenu();

        // אתחול רכיבים
        spType = findViewById(R.id.spType);
        sbMinutes = findViewById(R.id.sbMinutes);
        tvMinutes = findViewById(R.id.tvMinutes);
        Button btnSave = findViewById(R.id.btnSaveWorkout);

        // אתחול פיירבייס
        db = FirebaseFirestore.getInstance();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Toast.makeText(this, "אין משתמש מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // הגדרת סוגי אימונים (Spinner)
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Run", "Bike", "Strength"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        // האזנה לשינוי ב-SeekBar
        tvMinutes.setText(sbMinutes.getProgress() + " דקות");
        sbMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMinutes.setText(progress + " דקות");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // לחיצה על שמירה
        btnSave.setOnClickListener(v -> saveWorkoutToFirebase());
    }

    private void saveWorkoutToFirebase() {
        String type = spType.getSelectedItem().toString();
        int minutes = sbMinutes.getProgress();

        // חישוב ניקוד
        int multiplier = 1;
        switch (type) {
            case "Run": multiplier = 3; break;
            case "Bike": multiplier = 2; break;
            case "Strength": multiplier = 5; break;
        }
        int score = Math.max(1, minutes * multiplier);

        // --- כאן קורה הקסם בענן ---
        // אנחנו מעדכנים את המסמך של המשתמש בתיקיית users (או images, לפי מה שסידרנו ב-Main)
        db.collection("users").document(fbUser.getUid())
                .update(
                        "points", FieldValue.increment(score),           // הוספת ניקוד
                        "totalMinutes", FieldValue.increment(minutes),   // הוספת דקות
                        "workoutCount", FieldValue.increment(1)          // פלוס אימון אחד
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "אימון נשמר! +" + score + " נקודות", Toast.LENGTH_SHORT).show();
                    finish(); // סוגר את המסך וחוזר ל-Main
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}