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

    // משתנים - הורדתי את הקלוריות
    TextView tvName, tvEmail;
    TextView tvTotalMinutes, tvTotalWorkouts, tvTotalPoints;
    ImageView ivProfile;

    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_profile);
        setupMenu();

        initViews();

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            loadProfileData();
        }
    }

    private void initViews() {
        // חיבורים ל-XML
        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        ivProfile = findViewById(R.id.ivProfileImage);

        tvTotalMinutes = findViewById(R.id.tvTotalMinutes);
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        // הקלוריות נמחקו מפה
    }

    private void loadProfileData() {
        String uid = user.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {

                // שם ואימייל
                String name = documentSnapshot.getString("name");
                tvName.setText(name);
                tvEmail.setText(documentSnapshot.getString("email"));

                // נקודות - אם אין שם כלום נשים 0
                if(documentSnapshot.get("points") != null)
                    tvTotalPoints.setText(documentSnapshot.get("points") + "");
                else
                    tvTotalPoints.setText("0");

                // דקות
                long mins = 0;
                if(documentSnapshot.getLong("totalMinutes") != null)
                    mins = documentSnapshot.getLong("totalMinutes");
                tvTotalMinutes.setText(mins + "");

                // מספר אימונים
                tvTotalWorkouts.setText(documentSnapshot.get("workoutCount") + "");

                // תמונה
                String url = documentSnapshot.getString("avatarUri");
                if(url != null && !url.equals("")) {
                    Glide.with(getApplicationContext()).load(url).into(ivProfile);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "לא הצלחתי לטעון", Toast.LENGTH_SHORT).show();
        });
    }
}