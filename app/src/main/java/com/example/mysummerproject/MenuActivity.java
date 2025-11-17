package com.example.mysummerproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    protected void setupMenu() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // הגדרת header של המשתמש
        View headerView = navigationView.getHeaderView(0);
        ImageView navProfileImage = headerView.findViewById(R.id.navHeaderProfileImage);
        TextView navName = headerView.findViewById(R.id.navHeaderName);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) name = user.getEmail();

            navName.setText(name);

            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_profile)
                        .into(navProfileImage);
            } else {
                navProfileImage.setImageResource(R.drawable.default_profile);
            }
        } else {
            navName.setText("אין משתמש מחובר ❌");
            navProfileImage.setImageResource(R.drawable.default_profile);
        }

        // ניווט בין פריטים בתפריט
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_workouts)
                startActivity(new Intent(this, AddWorkoutActivity.class));
            else if (id == R.id.nav_login) startActivity(new Intent(this, LoginActivity2.class));
            else if (id == R.id.nav_help)
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://support.strava.com")));

            drawerLayout.closeDrawers();
            return true;
        });
    }
}
