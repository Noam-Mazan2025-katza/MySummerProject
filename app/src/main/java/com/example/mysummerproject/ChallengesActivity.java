package com.example.mysummerproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ChallengesAdapter adapter;
    private List<Challenge> challengeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // שים לב: אנחנו משתמשים רק ב-setLayout של ה-BaseActivity
        setContentLayout(R.layout.challenges);
        setupMenu();

        // איתחול הרשימה
        recyclerView = findViewById(R.id.rvChallenges);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        challengeList = new ArrayList<>();
        adapter = new ChallengesAdapter(challengeList);
        recyclerView.setAdapter(adapter);

        // טעינת הנתונים
        loadChallengesFromFirebase();

        // פונקציה זמנית - תפעיל אותה פעם אחת אם אין לך נתונים בכלל, ואז תמחק
        // addFakeDataToFirebase();
    }

    private void loadChallengesFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("challenges");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                challengeList.clear(); // ניקוי הרשימה לפני טעינה מחדש

                if (!snapshot.exists()) {
                    Toast.makeText(ChallengesActivity.this, "לא נמצאו אתגרים במערכת", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // 1. שליפת ה-ID והכותרת
                    String id = ds.getKey();
                    String title = ds.child("title").getValue(String.class);

                    // 2. שליפת הנתונים החדשים (עם בדיקה שלא נקרוס אם הם חסרים)
                    String tag = "כללי";
                    if (ds.child("tag").exists()) {
                        tag = ds.child("tag").getValue(String.class);
                    }

                    int points = 0;
                    if (ds.child("points").exists()) {
                        Integer p = ds.child("points").getValue(Integer.class);
                        if (p != null) points = p;
                    }

                    // 3. יצירת האובייקט עם כל המידע
                    Challenge challenge = new Challenge(id, title, tag, points);
                    challengeList.add(challenge);
                }

                // עדכון המסך
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChallengesActivity.this, "שגיאה בטעינה: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציית עזר להוספת נתונים ראשוניים (רק אם ה-Firebase ריק לגמרי)
    private void addFakeDataToFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("challenges");

        String id1 = ref.push().getKey();
        ref.child(id1).setValue(new Challenge(id1, "ריצת בוקר 5 ק״מ", "ריצה", 500));

        String id2 = ref.push().getKey();
        ref.child(id2).setValue(new Challenge(id2, "אימון משקולות", "כוח", 300));
    }
}