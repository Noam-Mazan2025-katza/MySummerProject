package com.example.mysummerproject;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends BaseActivity {

    // משתנים
    TextView tvName, tvEmail;
    TextView tvTotalMinutes, tvTotalWorkouts, tvTotalPoints;
    ImageView ivProfile;

    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // טעינת העיצוב והתפריט דרך BaseActivity
        setContentLayout(R.layout.activity_profile);
        setupMenu();

        initViews();

        // אתחול פיירבייס
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            loadData();
        } else {
            Toast.makeText(this, "אין משתמש מחובר", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        ivProfile = findViewById(R.id.ivProfileImage);

        tvTotalMinutes = findViewById(R.id.tvTotalMinutes);
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
    }

    private void loadData() {
        String uid = user.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {

                // שם ואימייל
                String name = document.getString("name");
                tvName.setText(name);
                tvEmail.setText(document.getString("email"));

                // נקודות - בדיקה פשוטה אם קיים
                if (document.get("points") != null)
                    tvTotalPoints.setText(document.get("points") + "");
                else
                    tvTotalPoints.setText("0");

                // דקות אימון
                if (document.get("totalMinutes") != null)
                    tvTotalMinutes.setText(document.get("totalMinutes") + "");
                else
                    tvTotalMinutes.setText("0");

                // מספר אימונים
                if (document.get("workoutCount") != null)
                    tvTotalWorkouts.setText(document.get("workoutCount") + "");
                else
                    tvTotalWorkouts.setText("0");

                // תמונה
                String url = document.getString("avatarUri");
                if (url != null && !url.equals("")) {
                    Glide.with(this).load(url).into(ivProfile);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
        });
    }
}