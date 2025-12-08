package com.example.mysummerproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChallengesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChallengesAdapter adapter;
    private List<Challenge> challengeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges); // העיצוב של המסך הראשי

        // איתחול הרשימה
        recyclerView = findViewById(R.id.rvChallenges);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        challengeList = new ArrayList<>();
        adapter = new ChallengesAdapter(challengeList);
        recyclerView.setAdapter(adapter);

        // טעינת הנתונים מ-Firebase
        loadChallengesFromFirebase();
    }

    private void loadChallengesFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("challenges");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                challengeList.clear(); // מנקים כדי לא ליצור כפילויות

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // שולפים את המידע הבסיסי
                    String id = ds.getKey();
                    String title = ds.child("title").getValue(String.class);

                    // מוסיפים לרשימה (המספר משתתפים יטען בנפרד באדפטר)
                    Challenge challenge = new Challenge(id, title);
                    challengeList.add(challenge);
                }

                // מודיעים לאדפטר שהמידע השתנה
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
