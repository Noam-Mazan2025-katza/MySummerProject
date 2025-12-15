package com.example.mysummerproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge currentChallenge = challengesList.get(position);

        // --- 1. הצגת נתונים בסיסיים (כותרת, תגית, נקודות) ---
        holder.tvTitle.setText(currentChallenge.getTitle());
        holder.tvTag.setText(currentChallenge.getTag());
        holder.tvPoints.setText("+" + currentChallenge.getPoints() + " נק'");

        // --- 2. עיצוב דינמי לפי סוג האתגר ---
        String tag = currentChallenge.getTag();
        if (tag != null && tag.equals("ריצה")) {
            holder.imgBackground.setImageResource(R.drawable.runner); // וודא שיש לך תמונה בשם runner בתיקיית drawable
            holder.tvTag.setBackgroundColor(0xFFFF9800); // כתום
        } else if (tag != null && tag.equals("כוח")) {
            holder.tvTag.setBackgroundColor(0xFFF44336); // אדום
            holder.imgBackground.setImageResource(R.drawable.images_power_training); // וודא שיש לך תמונה בשם runner בתיקיית drawable

        }else if (tag.equals("יוגה")) {
            holder.imgBackground.setImageResource(R.drawable.images_power_training); // עכשיו זה פעיל!
            holder.tvTag.setBackgroundColor(0xFF4CAF50); // ירוק
        }

        // --- 3. ניהול ה-Progress Bar ---
        holder.progressBar.setProgress(0);
        holder.tvProgressValue.setText("0%");

        // --- 4. חיבור ל-Firebase למספר המשתתפים ---
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("challenges")
                .child(currentChallenge.getId())
                .child("participants_count");

        // האזנה למספר בזמן אמת
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

        // --- 5. לחיצה על כפתור "עדכן/הצטרף" ---
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
                    // העדכון מתבצע אוטומטית בזכות ה-listener למעלה
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return challengesList.size();
    }

    // --- ה-ViewHolder המעודכן ---
    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvTag, tvPoints, tvProgressValue;
        Button btnAction;
        ImageView imgBackground;
        ProgressBar progressBar;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvProgressValue = itemView.findViewById(R.id.tvProgressValue); // חדש
            btnAction = itemView.findViewById(R.id.btnAction);
            imgBackground = itemView.findViewById(R.id.imgBackground);
            progressBar = itemView.findViewById(R.id.progressBar); // חדש
        }
    }
}