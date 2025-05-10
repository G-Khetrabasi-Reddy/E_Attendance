package com.example.e_attendance;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubjectRegistration extends AppCompatActivity {

    // Declare EditText and Button variables
    EditText ClassName, SemesterName, SubjectId1, SubjectId2, SubjectId3, SubjectId4, SubjectId5;
    EditText SubjectName1, SubjectName2, SubjectName3, SubjectName4, SubjectName5;
    Button add, remove;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_registration);

        // Initialize EditText and Button objects
        ClassName = findViewById(R.id.ClassName);
        SemesterName = findViewById(R.id.SemesterName);
        SubjectId1 = findViewById(R.id.SubjectId1);
        SubjectId2 = findViewById(R.id.SubjectId2);
        SubjectId3 = findViewById(R.id.SubjectId3);
        SubjectId4 = findViewById(R.id.SubjectId4);
        SubjectId5 = findViewById(R.id.SubjectId5);
        SubjectName1 = findViewById(R.id.SubjectName1);
        SubjectName2 = findViewById(R.id.SubjectName2);
        SubjectName3 = findViewById(R.id.SubjectName3);
        SubjectName4 = findViewById(R.id.SubjectName4);
        SubjectName5 = findViewById(R.id.SubjectName5);
        add = findViewById(R.id.Add);
        remove = findViewById(R.id.Remove);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("class");

        // Set click listener for add button using lambda expression
        add.setOnClickListener(v -> {
            String enteredClass = ClassName.getText().toString();
            String enteredSemester = SemesterName.getText().toString();

            // Check for empty class and semester names
            if (TextUtils.isEmpty(enteredClass) || TextUtils.isEmpty(enteredSemester)) {
                Toast.makeText(SubjectRegistration.this, "Please enter class and semester name!", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> subjectData = new HashMap<>();

            // Loop through Subject IDs and Names, adding non-empty entries to HashMap
            for (int i = 1; i <= 5; i++) {
                EditText subjectId = findViewById(getResources().getIdentifier("SubjectId" + i, "id", getPackageName()));
                EditText subjectName = findViewById(getResources().getIdentifier("SubjectName" + i, "id", getPackageName()));
                String subjectIdValue = subjectId.getText().toString();
                String subjectNameValue = subjectName.getText().toString();

                // Add only if both ID and Name are non-empty
                if (!TextUtils.isEmpty(subjectIdValue) && !TextUtils.isEmpty(subjectNameValue)) {
                    subjectData.put(subjectIdValue, subjectNameValue);
                }
            }

            // Check if subjectData has any entries before proceeding
            if (subjectData.isEmpty()) {
                Toast.makeText(SubjectRegistration.this, "Please enter details for at least one subject!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the class and semester exist, if not, create them
            DatabaseReference classRef = databaseReference.child(enteredClass).child(enteredSemester);
            classRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // Create the class and semester nodes
                        classRef.setValue(new HashMap<>())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Add subjects after creating the class and semester nodes
                                        classRef.updateChildren(subjectData)
                                                .addOnSuccessListener(unused -> Toast.makeText(SubjectRegistration.this, "Subjects added successfully!", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e -> Toast.makeText(SubjectRegistration.this, "Failed to add subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(SubjectRegistration.this, "Failed to create class and semester nodes", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If class and semester nodes already exist, directly add subjects
                        classRef.updateChildren(subjectData)
                                .addOnSuccessListener(unused -> Toast.makeText(SubjectRegistration.this, "Subjects added successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(SubjectRegistration.this, "Failed to add subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SubjectRegistration.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set click listener for remove button using lambda expression
        // Set click listener for remove button using lambda expression
        remove.setOnClickListener(v -> {
            String classToRemove = ClassName.getText().toString().trim();
            String semesterToRemove = SemesterName.getText().toString().trim();

            // Check if any fields are provided for removal
            if (TextUtils.isEmpty(classToRemove) || TextUtils.isEmpty(semesterToRemove)) {
                Toast.makeText(SubjectRegistration.this, "Please enter class and semester name for removal!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> subjectsToRemove = new ArrayList<>(); // List to store subject IDs to remove

            // Loop through Subject IDs and add non-empty IDs to the list
            for (int i = 1; i <= 5; i++) {
                EditText subjectId = findViewById(getResources().getIdentifier("SubjectId" + i, "id", getPackageName()));
                String subjectIdValue = subjectId.getText().toString().trim();
                if (!TextUtils.isEmpty(subjectIdValue)) {
                    subjectsToRemove.add(subjectIdValue);
                }
            }

            // Check if any subjects are provided for removal
            if (subjectsToRemove.isEmpty()) {
                Toast.makeText(SubjectRegistration.this, "Please enter at least one subject ID to remove!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove each subject from the database
            // Remove each subject from the database
            for (String subjectIdToRemove : subjectsToRemove) {
                // Construct the reference path for the subject to remove
                DatabaseReference subjectRef = databaseReference.child(classToRemove).child(semesterToRemove).child(subjectIdToRemove);

                // Check if the subject ID exists
                subjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Subject ID exists, proceed with removal
                            subjectRef.removeValue()
                                    .addOnSuccessListener(unused -> Toast.makeText(SubjectRegistration.this, "Subject removed successfully!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(SubjectRegistration.this, "Failed to remove subject: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            // Subject ID does not exist
                            Toast.makeText(SubjectRegistration.this, "Subject with ID " + subjectIdToRemove + " does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SubjectRegistration.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
