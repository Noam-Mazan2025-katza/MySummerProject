package com.example.mysummerproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.List;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder> {

    private List<Challenge> challengesList;

    public ChallengesAdapter(List<Challenge> challengesList) {
        this.challengesList = challengesList;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן אנחנו מחברים את העיצוב שיצרת (activity_item_challenge.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge currentChallenge = challengesList.get(position);

        // 1. הצגת הכותרת
        holder.tvTitle.setText(currentChallenge.getTitle());

        // 2. חיבור ל-Firebase למספר המשתתפים (לפי ה-ID של האתגר)
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("challenges")
                .child(currentChallenge.getId())
                .child("participants_count");

        // האזנה למספר בזמן אמת (Read)
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long count = snapshot.getValue(Long.class);
                    holder.tvSubtitle.setText("הצטרפו ל-" + count + " משתתפים");
                } else {
                    holder.tvSubtitle.setText("היה הראשון להצטרף!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. לחיצה על כפתור "הצטרף" (Write)
        holder.btnAction.setOnClickListener(v -> {
            ref.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer score = currentData.getValue(Integer.class);
                    if (score == null) {
                        currentData.setValue(1);
                    } else {
                        currentData.setValue(score + 1);
                    }
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    // כאן המספר כבר יתעדכן לבד בזכות ה-Listener למעלה
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return challengesList.size();
    }

    // המחלקה שמחזיקה את הרכיבים מהעיצוב שלך
    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvTag, tvPoints;
        Button btnAction;
        ImageView imgBackground;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle); // זה הטקסט של המשתתפים
            tvTag = itemView.findViewById(R.id.tvTag);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            btnAction = itemView.findViewById(R.id.btnAction);
            imgBackground = itemView.findViewById(R.id.imgBackground);
        }
    }
}