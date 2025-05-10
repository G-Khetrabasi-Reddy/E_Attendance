package com.example.e_attendance;

import android.content.Intent;
import android.os.Bundle;
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

public class TeacherRegistration extends AppCompatActivity {
    EditText TeacherId, username, password;
    Button Add, Remove,viewTeacherList;

    // Firebase database reference
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_registration);

        // Get reference to Firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Teacher");

        // Initialize EditText and Button objects
        TeacherId = findViewById(R.id.Teacherid);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        viewTeacherList = findViewById(R.id.ViewTeachers);
        Add = findViewById(R.id.Add);
        Remove = findViewById(R.id.Remove);

        // View teacher list action
        viewTeacherList.setOnClickListener(v -> startActivity(new Intent(TeacherRegistration.this, AvailableTeacherListByAdmin.class)));

        // Set click listener for Add button
        Add.setOnClickListener(v -> {
            String enteredTeacherId = TeacherId.getText().toString();
            String enteredUsername = username.getText().toString();
            String enteredPassword = password.getText().toString();

            // Check if all fields are filled
            if (enteredTeacherId.isEmpty() || enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(TeacherRegistration.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the teacher ID already exists
            databaseReference.child(enteredTeacherId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Teacher ID does not exist, add the teacher
                        databaseReference.child(enteredTeacherId).child("username").setValue(enteredUsername);
                        databaseReference.child(enteredTeacherId).child("password").setValue(enteredPassword);
                        Toast.makeText(TeacherRegistration.this, "Teacher added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Teacher ID already exists
                        Toast.makeText(TeacherRegistration.this, "User already Exists", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TeacherRegistration.this, "Error adding Teacher", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set click listener for Remove button
        Remove.setOnClickListener(v -> {
            String enteredTeacherId = TeacherId.getText().toString();

            // Check if Teacher ID field is empty
            if (enteredTeacherId.isEmpty()) {
                Toast.makeText(TeacherRegistration.this, "Please enter Teacher ID to Remove Teacher", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the entered Teacher ID exists before removing
            databaseReference.child(enteredTeacherId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Teacher ID exists, remove the teacher
                        databaseReference.child(enteredTeacherId).removeValue();
                        Toast.makeText(TeacherRegistration.this, "Teacher removed Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Teacher ID doesn't exist
                        Toast.makeText(TeacherRegistration.this, "Teacher not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TeacherRegistration.this, "Error removing Teacher", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
