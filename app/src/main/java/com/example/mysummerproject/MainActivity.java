package com.example.mysummerproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout usersContainer;
    private ListenerRegistration usersListener;

    private TextView tvWelcome;
    private ImageView imgProfile;
    private Button btnLogout, btnAddUser, btnAddWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.activity_main);
        setupMenu();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usersContainer = findViewById(R.id.usersContainer);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        FloatingActionButton fabDelete = findViewById(R.id.fabDelete);

        tvWelcome = findViewById(R.id.tvWelcome);
        imgProfile = findViewById(R.id.imgProfile);
        btnLogout = findViewById(R.id.btnLogout);

        updateUserUI();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUserUI();
            setupMenu();
        });

        btnAddUser.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity2.class)));
        btnAddWorkout.setOnClickListener(v -> startActivity(new Intent(this, AddWorkoutActivity.class)));

        // הפעלת המאזין
        startListening();

        fabDelete.setOnClickListener(v -> Toast.makeText(this, "מחיקה תתבצע דרך פיירבייס", Toast.LENGTH_SHORT).show());
    }

    private void startListening() {
        if (usersContainer == null) return;

        // --- שינוי 1: הוספנו .orderBy כדי שהטבלה תהיה ממוינת לפי נקודות (מהגבוה לנמוך) ---
        usersListener = db.collection("users")
                .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        android.util.Log.e("Firebase", "Error listening", e);
                        return;
                    }

                    if (snapshots != null) {
                        usersContainer.removeAllViews();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String uid = doc.getId();
                            String name = doc.getString("name");
                            String base64Image = doc.getString("imageData");

                            // שליפת נקודות
                            long pts = 0;
                            if (doc.contains("points") && doc.get("points") != null) {
                                pts = doc.getLong("points");
                            }

                            View card = getLayoutInflater().inflate(R.layout.view_user_card, usersContainer, false);
                            ImageView iv = card.findViewById(R.id.ivAvatar);
                            TextView tvName = card.findViewById(R.id.tvName);
                            TextView tvPts = card.findViewById(R.id.tvPoints);

                            tvName.setText(name);
                            tvPts.setText(pts + " נק׳");

                            // הצגת תמונה (כבר עובד לך)
                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    iv.setImageBitmap(bitmap);
                                } catch (Exception ex) {
                                    iv.setImageResource(R.drawable.default_profile);
                                }
                            }

                            // --- שינוי 2: עדכון נקודות חכם ---
                            card.setOnClickListener(v -> {
                                // שימוש ב-FieldValue.increment(1) - הדרך הכי בטוחה להוסיף נקודות
                                db.collection("users").document(uid)
                                        .update("points", com.google.firebase.firestore.FieldValue.increment(1))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "נוספה נקודה ל-" + name, Toast.LENGTH_SHORT).show();
                                        });
                            });

                            usersContainer.addView(card);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserUI();
        if (usersListener == null) startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }
    }

    private void updateUserUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (btnAddUser != null) btnAddUser.setVisibility(View.GONE);
            if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);
            tvWelcome.setText("שלום " + (user.getDisplayName() != null ? user.getDisplayName() : user.getEmail()));
            if (imgProfile != null) {
                if (user.getPhotoUrl() != null) Glide.with(this).load(user.getPhotoUrl()).into(imgProfile);
                else imgProfile.setImageResource(R.drawable.default_profile);
            }
        } else {
            if (btnAddUser != null) btnAddUser.setVisibility(View.VISIBLE);
            if (btnLogout != null) btnLogout.setVisibility(View.GONE);
            tvWelcome.setText("אין משתמש מחובר");
            imgProfile.setImageResource(R.drawable.default_profile);
        }
    }
}