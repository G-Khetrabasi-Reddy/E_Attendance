package com.example.e_attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AvailableSubjectListByAdmin extends AppCompatActivity {

    private LinearLayout subjectContainer;
    private String selectedClass, selectedSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_subject_list_by_admin);

        // Find the container for subjects
        subjectContainer = findViewById(R.id.subject_container);

        // Retrieve class and semester values from ViewStudentsAndSubjectsByAdmin
        selectedClass = getIntent().getStringExtra("class");
        selectedSemester = getIntent().getStringExtra("semester");

        // Get subject list from Firebase based on selected class and semester
        retrieveSubjectsFromFirebase();
    }

    private void retrieveSubjectsFromFirebase() {
        FirebaseDatabase.getInstance().getReference("class").child(selectedClass)
                .child(selectedSemester).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                            String subjectId = subjectSnapshot.getKey();
                            String subjectName = subjectSnapshot.getValue(String.class);

                            // Create a card view for the subject
                            View cardView = LayoutInflater.from(AvailableSubjectListByAdmin.this)
                                    .inflate(R.layout.subject_list_card_view_by_admin, subjectContainer, false);

                            // Set subject details in the card view
                            TextView subjectIdTextView = cardView.findViewById(R.id.SubjectId);
                            subjectIdTextView.setText(subjectId);
                            TextView subjectNameTextView = cardView.findViewById(R.id.SubjectName);
                            subjectNameTextView.setText(subjectName);

                            // Add the card view to the subject container
                            subjectContainer.addView(cardView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }
}
