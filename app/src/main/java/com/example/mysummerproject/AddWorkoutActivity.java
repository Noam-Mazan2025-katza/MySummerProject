package com.example.mysummerproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AddWorkoutActivity extends BaseActivity {

    private Spinner spType;
    private SeekBar sbMinutes;
    private TextView tvMinutes;

    private FirebaseUser fbUser;
    private String loggedUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ❗ רק זה! לא setContentView פעמיים
        setContentLayout(R.layout.activity_add_workout);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setupMenu();

        spType = findViewById(R.id.spType);
        sbMinutes = findViewById(R.id.sbMinutes);
        tvMinutes = findViewById(R.id.tvMinutes);
        Button btnSave = findViewById(R.id.btnSaveWorkout);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "אין משתמש מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loggedUserEmail = fbUser.getEmail();

        // סוגי אימונים
        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        new String[]{"Run", "Bike", "Strength"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        // עדכון טקסט הדקות
        tvMinutes.setText(sbMinutes.getProgress() + " דקות");

        sbMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMinutes.setText(progress + " דקות");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // שמירת אימון
        btnSave.setOnClickListener(v -> {

            String type = spType.getSelectedItem().toString();
            int minutes = sbMinutes.getProgress();

            int multiplier = 1;
            switch (type) {
                case "Run": multiplier = 3; break;
                case "Bike": multiplier = 2; break;
                case "Strength": multiplier = 5; break;
            }

            int score = Math.max(1, minutes * multiplier);

            // מוסיף נקודות למשתמש שמחובר
            String loggedUserName = fbUser.getDisplayName();
            if (loggedUserName == null || loggedUserName.isEmpty()) {
                loggedUserName = fbUser.getEmail(); // או תן למשתמש שם קודם
            }
            PrefsRepo.addPoints(this, loggedUserName, score);


            Toast.makeText(this, "נשמר: +" + score + " נקודות", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
