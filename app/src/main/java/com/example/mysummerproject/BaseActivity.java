package com.example.mysummerproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // טוען את ה-Base Layout עם Drawer
        setContentView(R.layout.base_layout); // <-- הקובץ שלך עם Drawer ו-FrameLayout
        setupMenu();
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

        // Header + Firebase
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

        // Navigation Drawer items
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_workouts) startActivity(new Intent(this, AddWorkoutActivity.class));
            else if (id == R.id.nav_login) startActivity(new Intent(this, LoginActivity2.class));
            else if (id == R.id.nav_register) startActivity(new Intent(this, Register.class));
            else if (id == R.id.nav_help)
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://support.strava.com")));

            drawerLayout.closeDrawers();
            return true;
        });
    }

    // הפונקציה שמטעינה תוכן מסך ספציפי בתוך ה-FrameLayout
    protected void setContentLayout(int layoutResID) {
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.main_content), true);
    }
}


