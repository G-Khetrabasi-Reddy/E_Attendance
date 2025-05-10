package com.example.e_attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AvailableTeacherListByAdmin extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_teacher_list_by_admin);

        // Initialize Firebase database reference
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Teacher");

        // Find the container layout for teachers
        LinearLayout teacherContainer = findViewById(R.id.teacher_container);

        // Read data from the Teacher node in Firebase Realtime Database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the container before adding new data
                teacherContainer.removeAllViews();

                for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve teacher ID and name from Firebase snapshot
                    String teacherId = teacherSnapshot.getKey();
                    String teacherName = teacherSnapshot.child("username").getValue(String.class);

                    // Inflate the teacher_card_view_list_by_admin layout for each teacher
                    CardView teacherCardView = (CardView) LayoutInflater.from(AvailableTeacherListByAdmin.this)
                            .inflate(R.layout.teacher_card_view_list_by_admin, teacherContainer, false);

                    // Find and set teacher ID and name in the card view
                    TextView teacherIdTextView = teacherCardView.findViewById(R.id.TeacherId);
                    teacherIdTextView.setText(teacherId);

                    TextView teacherNameTextView = teacherCardView.findViewById(R.id.TeacherName);
                    teacherNameTextView.setText(teacherName);

                    // Add the card view to the container
                    teacherContainer.addView(teacherCardView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AvailableTeacherListByAdmin.this, "Error retrieving Teacher's List: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
