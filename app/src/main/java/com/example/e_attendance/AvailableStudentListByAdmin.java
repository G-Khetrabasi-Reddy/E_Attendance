package com.example.e_attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AvailableStudentListByAdmin extends AppCompatActivity {
    private LinearLayout studentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_student_list_by_admin);

        // Find the container for students
        studentContainer = findViewById(R.id.student_container);

        // Retrieve class and semester values from ViewStudentsAndSubjectsByAdmin
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String selectedClass = extras.getString("class");
            String selectedSemester = extras.getString("semester");

            // Initialize ArrayList to store student IDs
            final ArrayList<String> studentIds = new ArrayList<>();

            // Query Firebase to retrieve student IDs where class and semester match selected values
            Query query = FirebaseDatabase.getInstance().getReference("Student")
                    .orderByChild("class").equalTo(selectedClass);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        String semester = studentSnapshot.child("semester").getValue(String.class);
                        if (semester != null && semester.equals(selectedSemester)) { // Check for null before calling equals
                            studentIds.add(studentId);
                        }
                    }

                    // Add card views for each student
                    for (String studentId : studentIds) {
                        addStudentCardView(studentId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }

    // Method to add a card view for each student
    private void addStudentCardView(String studentId) {
        // Inflate student card view layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.student_card_view_list_by_admin, studentContainer, false);

        // Set serial number
        TextView serialNumberTextView = cardView.findViewById(R.id.serial_number_text_view);
        serialNumberTextView.setText(String.format("%02d", studentContainer.getChildCount() + 1));
        // Set student ID
        TextView studentIdTextView = cardView.findViewById(R.id.student_id_text_view);
        studentIdTextView.setText(studentId);

        // Retrieve and set student username from Firebase
        FirebaseDatabase.getInstance().getReference("Student").child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    if (username != null) {
                        TextView studentNameTextView = cardView.findViewById(R.id.student_name_text_view);
                        studentNameTextView.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Add margin to card view
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(layoutParams);

        // Add card view to container
        studentContainer.addView(cardView);
    }
}
