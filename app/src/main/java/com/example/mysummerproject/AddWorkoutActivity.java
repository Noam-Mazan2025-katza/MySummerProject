package com.example.mysummerproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AddWorkoutActivity extends BaseActivity {
    private Spinner spUser, spType;
    private SeekBar sbMinutes;
    private TextView tvMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_add_workout);


        spUser = findViewById(R.id.spUser);
        spType = findViewById(R.id.spType);
        sbMinutes = findViewById(R.id.sbMinutes);
        tvMinutes = findViewById(R.id.tvMinutes);
        Button btnSave = findViewById(R.id.btnSaveWorkout); // ← היה בהערה: להחזיר!



        // משתמשים מהרשימה השמורה
        List<String> names = PrefsRepo.getUserNames(this);
        if (names.isEmpty()) {
            Toast.makeText(this, "אין משתמשים. צור משתמש קודם.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // מתאם ל-Spinner של משתמשים
        ArrayAdapter<String> usersAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUser.setAdapter(usersAdapter);

        // סוגי אימון
        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        new String[]{"Run", "Bike", "Strength"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        // עדכון טקסט הדקות
        tvMinutes.setText(sbMinutes.getProgress() + " דקות");
        sbMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
                tvMinutes.setText(p + " דקות");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(v -> {
            String user = (String) spUser.getSelectedItem();
            int minutes = sbMinutes.getProgress();
            int score = Math.max(1, minutes); // מינימום נקודה אחת

            PrefsRepo.addPoints(this, user, score);
            if (minutes >= 30) {
                PrefsRepo.setBadge(this, user, true); // מעניק Badge לדוגמה
            }

            // עדיף להשתמש במחרוזת מהמשאבים או עברית ישירה בקידוד UTF-8
            Toast.makeText(this, "נשמר: +" + score + " נק׳ ל-" + user, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

}
