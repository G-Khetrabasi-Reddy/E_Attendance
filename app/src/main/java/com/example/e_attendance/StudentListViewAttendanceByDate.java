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

public class StudentListViewAttendanceByDate extends AppCompatActivity {

    private LinearLayout studentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_view_attendance_by_date);

        // Initialize linear layout for student cards
        studentContainer = findViewById(R.id.student_view_container);

        // Retrieve subject ID and selected date from intent
        String subjectId = getIntent().getStringExtra("subject_id");
        String selectedDate = getIntent().getStringExtra("date");

        // Check if subjectId and selectedDate are null
        if (subjectId == null || selectedDate == null) {
            // Handle the case where subjectId or selectedDate is null
            // (e.g., unexpected intent extras)
            return;
        }

        // Query Firebase to retrieve attendance records for the subject and date
        Query query = FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                .child(subjectId)
                .child(selectedDate);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each student's attendance record
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve student ID and attendance status
                    String studentId = studentSnapshot.getKey();
                    String status = studentSnapshot.getValue(String.class);

                    // Add student card view to the layout
                    addStudentCardView(studentId, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors (e.g., Firebase query cancellation)
            }
        });

    }

    // Method to add a card view for a student with attendance details
    private void addStudentCardView(String studentId, String status) {
        // Inflate student card view layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.student_card_view_attendance_by_date, null);

        // Set serial number
        TextView serialNumberTextView = cardView.findViewById(R.id.serial_number_text_view);
        serialNumberTextView.setText(String.format("%02d", studentContainer.getChildCount() + 1));

        // Set student ID
        TextView studentIdTextView = cardView.findViewById(R.id.student_id_text_view);
        studentIdTextView.setText(studentId);

        // Set attendance status
        TextView statusTextView = cardView.findViewById(R.id.status_text_view);
        statusTextView.setText(status);

        // Change card color based on attendance status
        if ("Present".equals(status)) {
            cardView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            cardView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }

        // Add margin to card view
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(layoutParams);

        // Add card view to the container
        studentContainer.addView(cardView);
    }
}
