package com.example.mysummerproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // משתנים שקשורים למשתמשים ולאימונים
    private LinearLayout usersContainer;
    private final List<String> selectedUsers = new ArrayList<>();
    private final Set<String> selectedNames = new HashSet<>();

    // רכיבים חדשים של המשתמש המחובר
    private TextView tvWelcome;
    private ImageView imgProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול רכיבי המסך
        usersContainer = findViewById(R.id.usersContainer);
        Button btnAddUser = findViewById(R.id.btnAddUser);
        Button btnAddWorkout = findViewById(R.id.btnAddWorkout);
        FloatingActionButton fabDelete = findViewById(R.id.fabDelete);

        // 🟢 רכיבי המשתמש המחובר
        tvWelcome = findViewById(R.id.tvWelcome);
        imgProfile = findViewById(R.id.imgProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // --------------------------------------------------
        // חלק א' — בדיקת מצב המשתמש המחובר
        // --------------------------------------------------
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            user.reload();
            tvWelcome.setText("שלום " + name);

            // 🔹 הצגת תמונת הפרופיל באמצעות Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_profile)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.default_profile);
            }

            btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvWelcome.setText("אין משתמש מחובר ❌");
            imgProfile.setImageResource(R.drawable.default_profile);
            btnLogout.setVisibility(View.GONE);
        }

        // --------------------------------------------------
        // חלק ב' — פעולות התחברות / התנתקות
        // --------------------------------------------------
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            tvWelcome.setText("אין משתמש מחובר ❌");
            imgProfile.setImageResource(R.drawable.default_profile);
            btnLogout.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        });

        // --------------------------------------------------
        // חלק ג' — ניווט בין מסכים
        // --------------------------------------------------
        btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity2.class))
        );

        btnAddWorkout.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class))
        );

        // --------------------------------------------------
        renderLeaderboard();
        // --------------------------------------------------
        // חלק ה' — כפתור מחיקה
        // --------------------------------------------------
        fabDelete.setOnClickListener(v -> {
            for (String name : new ArrayList<>(selectedUsers)) {
                PrefsRepo.removeUsers(this, Collections.singleton(name));
            }
            PrefsRepo.removeUsers(this, selectedNames);
            selectedNames.clear();
            selectedUsers.clear();
            renderLeaderboard();
            Toast.makeText(this, "המשתמשים נמחקו", Toast.LENGTH_SHORT).show();
        });
    }

    // --------------------------------------------------
    // חלק ו' — רענון הנתונים
    // --------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        renderLeaderboard();
    }

    // --------------------------------------------------
    // חלק ז' — יצירת רשימת המשתמשים
    // --------------------------------------------------
    private void renderLeaderboard() {
        usersContainer.removeAllViews();
        for (PrefsRepo.User u : PrefsRepo.getUsersSorted(this)) {
            View card = getLayoutInflater().inflate(R.layout.view_user_card, usersContainer, false);

            ImageView iv = card.findViewById(R.id.ivAvatar);
            TextView tvName = card.findViewById(R.id.tvName);
            TextView tvPts = card.findViewById(R.id.tvPoints);
            TextView tvBadge = card.findViewById(R.id.tvBadge);

            tvName.setText(u.name);
            tvPts.setText(u.points + " נק׳");

            // 🔹 שימוש ב-Glide להצגת תמונת המשתמש בריבועים
            if (u.avatarUri != null) {
                Glide.with(this)
                        .load(u.avatarUri)
                        .placeholder(R.drawable.default_profile)
                        .into(iv);
            } else {
                iv.setImageResource(R.drawable.default_profile);
            }

            tvBadge.setVisibility(u.badge ? View.VISIBLE : View.GONE);

            card.setOnClickListener(v -> {
                PrefsRepo.addPoints(this, u.name, 1);
                renderLeaderboard();
            });

            CheckBox cb = card.findViewById(R.id.cbSelect);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedUsers.add(u.name);
                else selectedUsers.remove(u.name);
            });

            usersContainer.addView(card);
        }
    }

    // --------------------------------------------------
    // חלק ח' — תפריט הגדרות
    // --------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // בעתיד: startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
