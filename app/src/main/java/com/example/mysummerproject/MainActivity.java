package com.example.mysummerproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64; // חשוב להוספת תמונות
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot; // חשוב
import com.google.firebase.firestore.FirebaseFirestore; // חשוב

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity {

    // משתנים
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // הוספנו את זה בשביל למשוך נתונים
    private LinearLayout usersContainer;

    // רשימות למחיקה ולטבלה (נשאיר אותן כדי שהקוד לא ישבר, אבל השימוש בהן השתנה)
    private final List<String> selectedUsers = new ArrayList<>();
    private final Set<String> selectedNames = new HashSet<>();

    // רכיבים במסך הראשי
    private TextView tvWelcome;
    private ImageView imgProfile;
    private Button btnLogout, btnAddUser, btnAddWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. טעינת העיצוב והפעלת התפריט דרך BaseActivity
        setContentLayout(R.layout.activity_main);
        setupMenu();

        // עיצוב (מחיקת סטטוס בר)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // 2. אתחול משתנים
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // אתחול מסד הנתונים

        usersContainer = findViewById(R.id.usersContainer);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        FloatingActionButton fabDelete = findViewById(R.id.fabDelete);

        tvWelcome = findViewById(R.id.tvWelcome);
        imgProfile = findViewById(R.id.imgProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // עדכון ראשוני
        updateUserUI();

        // 3. כפתורים

        // התנתקות
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "התנתקת", Toast.LENGTH_SHORT).show();
            updateUserUI();
            setupMenu(); // מעדכן גם את התפריט בצד
            renderLeaderboard(); // מרענן את הרשימה
        });

        btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity2.class))
        );

        btnAddWorkout.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class))
        );

        // טבלה - עכשיו טוענת מפיירבייס
        renderLeaderboard();

        // מחיקה - כרגע רק מנקה את המסך (כי אנחנו לא מוחקים מהענן בלחיצה הזו)
        fabDelete.setOnClickListener(v -> {
            usersContainer.removeAllViews();
            Toast.makeText(this, "הרשימה נוקתה מהמסך", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserUI();
        renderLeaderboard(); // טוען מחדש כשחוזרים
        setupMenu();
    }

    private void updateUserUI() {
        FirebaseUser user = mAuth.getCurrentUser();

        // עדכון המסך הראשי בלבד (התפריט מטופל ב-BaseActivity)
        if (user != null) {
            if (btnAddUser != null) btnAddUser.setVisibility(View.GONE);
            if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);

            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) name = user.getEmail();

            if (tvWelcome != null) tvWelcome.setText("שלום " + name);

            if (imgProfile != null) {
                if (user.getPhotoUrl() != null) {
                    Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.default_profile).into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.default_profile);
                }
            }
        } else {
            if (btnAddUser != null) btnAddUser.setVisibility(View.VISIBLE);
            if (btnLogout != null) btnLogout.setVisibility(View.GONE);
            if (tvWelcome != null) tvWelcome.setText("אין משתמש מחובר");
            if (imgProfile != null) imgProfile.setImageResource(R.drawable.default_profile);
        }
    }

    // --- הפונקציה ששונתה: טוענת מ-Firebase images ---
    private void renderLeaderboard() {
        if (usersContainer == null) return;

        usersContainer.removeAllViews();

        // במקום PrefsRepo, אנחנו ניגשים לתיקיית "images" בפיירבייס
        db.collection("images").get().addOnSuccessListener(queryDocumentSnapshots -> {

            // עובר על כל המשתמשים שנמצאו
            for (DocumentSnapshot doc : queryDocumentSnapshots) {

                // שליפת המידע
                String name = doc.getString("name");
                String base64Image = doc.getString("imageData"); // התמונה המוצפנת

                // יצירת הכרטיס
                View card = getLayoutInflater().inflate(R.layout.view_user_card, usersContainer, false);

                ImageView iv = card.findViewById(R.id.ivAvatar);
                TextView tvName = card.findViewById(R.id.tvName);
                TextView tvPts = card.findViewById(R.id.tvPoints);
                TextView tvBadge = card.findViewById(R.id.tvBadge);
                CheckBox cb = card.findViewById(R.id.cbSelect);

                tvName.setText(name);
                tvPts.setText("0 נק׳"); // ב-Register המקורי אין נקודות, אז נשים 0 בינתיים
                tvBadge.setVisibility(View.GONE); // נסתיר את התג בינתיים

                // פענוח התמונה (כי שמרת אותה כ-Base64 ולא כ-URL)
                if (base64Image != null && !base64Image.isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        iv.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        iv.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    iv.setImageResource(R.drawable.default_profile);
                }

                // לחיצה על הכרטיס
                card.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "משתמש: " + name, Toast.LENGTH_SHORT).show();
                });

                // הוספה למסך
                usersContainer.addView(card);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "לא הצלחתי לטעון רשימה", Toast.LENGTH_SHORT).show();
        });
    }
}