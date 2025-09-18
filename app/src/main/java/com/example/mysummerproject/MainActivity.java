package com.example.mysummerproject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private LinearLayout usersContainer;
    private Switch swMusic;
    private final List<String> selectedUsers = new ArrayList<>();
    private final Set<String> selectedNames = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersContainer = findViewById(R.id.usersContainer);
        swMusic = findViewById(R.id.swMusic);
        Button btnAddUser = findViewById(R.id.btnAddUser);
        Button btnAddWorkout = findViewById(R.id.btnAddWorkout);

        // כפתור +משתמש → פותח את מסך הוספת משתמש
        btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity2.class))
        );


        btnAddWorkout.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class))
        );


        swMusic.setOnClickListener(v -> {
            boolean on = swMusic.isChecked();
            Intent s = new Intent(this, MusicService.class);
            if (on) {
                startService(s);
            } else {
                stopService(s);
            }
        });

        // אם אין נתונים—ממלאים דמו פעם ראשונה
        if (PrefsRepo.getUserNames(this).isEmpty()) {
            PrefsRepo.addUser(this, "נועה", null);
            PrefsRepo.addUser(this, "דוד", null);
            PrefsRepo.addUser(this, "מאיה", null);
            PrefsRepo.addPoints(this, "דוד", 20);
            PrefsRepo.setBadge(this, "דוד", true);
        }
        FloatingActionButton fabDelete = findViewById(R.id.fabDelete);
        fabDelete.setOnClickListener(v -> {
            for (String name : new ArrayList<>(selectedUsers)) {
                PrefsRepo.removeUsers(this, Collections.singleton(name));
            }
            PrefsRepo.removeUsers(this, selectedNames);
            selectedNames.clear();
            renderLeaderboard();
            selectedUsers.clear();
            renderLeaderboard();
            Toast.makeText(this, "המשתמשים נמחקו", Toast.LENGTH_SHORT).show();
        });

    }

    @Override protected void onResume() {
        super.onResume();
        renderLeaderboard();
    }

    private void renderLeaderboard() {
        usersContainer.removeAllViews();
        for (PrefsRepo.User u : PrefsRepo.getUsersSorted(this)) {
            View card = getLayoutInflater().inflate(R.layout.view_user_card, usersContainer, false);

            ImageView iv   = card.findViewById(R.id.ivAvatar);
            TextView tvName = card.findViewById(R.id.tvName);
            TextView tvPts  = card.findViewById(R.id.tvPoints);
            TextView tvBadge= card.findViewById(R.id.tvBadge);


            tvName.setText(u.name);
            tvPts.setText(u.points + " נק׳");
            if (u.avatarUri != null) iv.setImageURI(android.net.Uri.parse(u.avatarUri));
            tvBadge.setVisibility(u.badge ? View.VISIBLE : View.GONE);

            // “עידוד” – לחיצה מוסיפה נקודה ושומרת
            card.setOnClickListener(v -> {
                PrefsRepo.addPoints(this, u.name, 1);
                renderLeaderboard();
            });
            CheckBox cb = card.findViewById(R.id.cbSelect);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUsers.add(u.name);
                } else {
                    selectedUsers.remove(u.name);
                }
            });



            usersContainer.addView(card);
        }
    }


    // תפריט (נשתמש בהמשך להגדרות)
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}