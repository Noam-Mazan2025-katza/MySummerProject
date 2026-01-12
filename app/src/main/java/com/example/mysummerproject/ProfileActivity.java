package com.example.mysummerproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// שים לב: שינינו את הירושה ל-BaseActivity כדי לקבל את התפריט
public class ProfileActivity extends BaseActivity {

    // --- משתני UI (ממשק) ---
    private TextView tvName, tvEmail;
    private TextView tvRankName, tvPointsStatus;

    // ארבעת הריבועים לסטטיסטיקה
    private TextView tvTotalMinutes, tvTotalWorkouts, tvTotalCalories, tvTotalPoints;

    private ImageView ivProfile;
    private ProgressBar pbLevel;
    private Button btnEditProfile;

    // --- משתני Firebase ---
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // במקום setContentView רגיל, אנחנו משתמשים בפונקציה של BaseActivity
        // כדי שהעיצוב יכנס לתוך התבנית עם התפריט צד
        setContentLayout(R.layout.activity_profile);
        setupMenu();

        // 1. אתחול Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // 2. קישור המשתנים לרכיבים ב-XML
        initViews();

        // 3. הגדרת כפתור עריכה (אופציונלי - כרגע רק הודעה)
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Toast.makeText(this, "אפשרות עריכה תתווסף בקרוב", Toast.LENGTH_SHORT).show();
            });
        }

        // 4. טעינת הנתונים אם המשתמש מחובר
        if (user != null) {
            loadUserProfile();
        } else {
            Toast.makeText(this, "נא להתחבר למערכת", Toast.LENGTH_SHORT).show();
            // אם אין משתמש, מחזירים למסך כניסה
            startActivity(new Intent(this, LoginActivity2.class));
            finish();
        }
    }

    // פונקציה שמקשרת את הקוד ל-IDs בקובץ ה-XML
    private void initViews() {
        // פרטים אישיים
        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        ivProfile = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // דרגות והתקדמות
        tvRankName = findViewById(R.id.tvRankName);
        tvPointsStatus = findViewById(R.id.tvPointsStatus);
        pbLevel = findViewById(R.id.pbLevel);

        // סטטיסטיקות (4 הריבועים)
        tvTotalMinutes = findViewById(R.id.tvTotalMinutes);
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
    }

    /**
     * פונקציה לשליפת הנתונים מ-Firestore.
     * עונה על דרישת המחוון: "שימוש בבסיס נתונים" ו"אחסון נתונים".
     */
    private void loadUserProfile() {
        // גישה למסמך המשתמש באוסף "users" לפי ה-ID הייחודי שלו
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        updateUI(document); // עדכון המסך עם המידע
                    } else {
                        Toast.makeText(this, "עדיין לא נוצר פרופיל למשתמש זה", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error loading data", e);
                    Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * עדכון ה-UI (הממשק הגרפי) עם הנתונים שהתקבלו
     */
    private void updateUI(DocumentSnapshot doc) {
        // --- 1. טעינת פרטים בסיסיים ---
        String name = doc.getString("name");
        String email = user.getEmail();

        tvName.setText(name != null ? name : "משתמש אנונימי");
        tvEmail.setText(email);

        // --- 2. טעינת מספרים וסטטיסטיקות ---
        // שימוש ב-getLong עם בדיקת null כדי למנוע קריסה אם הנתון חסר
        long points = doc.getLong("points") != null ? doc.getLong("points") : 0;
        long minutes = doc.getLong("totalMinutes") != null ? doc.getLong("totalMinutes") : 0;
        long workouts = doc.getLong("workoutCount") != null ? doc.getLong("workoutCount") : 0;
        long calories = doc.getLong("totalCalories") != null ? doc.getLong("totalCalories") : 0;

        // הצגת המספרים
        tvTotalPoints.setText(String.valueOf(points));
        tvTotalMinutes.setText(String.valueOf(minutes));
        tvTotalWorkouts.setText(String.valueOf(workouts));
        tvTotalCalories.setText(String.valueOf(calories));

        // --- 3. חישוב דרגה (Gamification) ---
        calculateRank(points);

        // --- 4. טעינת תמונת פרופיל ---
        String photoUrl = doc.getString("avatarUri");

        // עדיפות לתמונה ששמרנו ב-Firestore, אחרת מנסים מ-Google Auth, ואם אין - תמונת ברירת מחדל
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this).load(Uri.parse(photoUrl)).placeholder(R.drawable.default_profile).into(ivProfile);
        } else if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.default_profile).into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.default_profile);
        }
    }

    /**
     * לוגיקה לחישוב הדרגה ומד ההתקדמות.
     * כל 100 נקודות מעלות רמה.
     */
    private void calculateRank(long points) {
        int pointsPerLevel = 100; // כמה נקודות צריך כדי לעלות רמה

        // חישוב הרמה הנוכחית
        int currentLevel = (int) (points / pointsPerLevel) + 1;

        // חישוב כמה נקודות צברנו בתוך הרמה הנוכחית (השארית)
        int progressInLevel = (int) (points % pointsPerLevel);

        // קביעת טקסט הדרגה
        String rankTitle;
        if (currentLevel == 1) rankTitle = "מתחיל (רמה 1)";
        else if (currentLevel == 2) rankTitle = "מתקדם (רמה 2)";
        else if (currentLevel == 3) rankTitle = "מקצוען (רמה 3)";
        else if (currentLevel == 4) rankTitle = "אלוף (רמה 4)";
        else rankTitle = "אגדה (רמה " + currentLevel + ")";

        // עדכון המסך
        tvRankName.setText("דרגה: " + rankTitle);

        pbLevel.setMax(pointsPerLevel); // המקסימום בפס הוא 100
        pbLevel.setProgress(progressInLevel); // המיקום הנוכחי בפס

        tvPointsStatus.setText(progressInLevel + "/" + pointsPerLevel + " לדרגה הבאה");
    }
}