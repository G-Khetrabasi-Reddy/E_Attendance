package com.example.e_attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentRegistration extends AppCompatActivity {

    // Declare edit text and button variables
    EditText RegdNo, username, password;
    Button Add, Remove;

    // Declare spinner and adapter variables
    Spinner ClassSpinner, SemesterSpinner;

    // Firebase database reference
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Student");

        // Initialize edit text and button objects
        RegdNo = findViewById(R.id.RegdNo);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        ClassSpinner  = findViewById(R.id.Classes);
        SemesterSpinner = findViewById(R.id.Semesters);
        Add = findViewById(R.id.Add);
        Remove = findViewById(R.id.Remove);

        // Initialize adapters
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        ClassSpinner.setAdapter(classAdapter);
        SemesterSpinner.setAdapter(semesterAdapter);

        // Retrieve class list from Firebase
        FirebaseDatabase.getInstance().getReference("class").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    String className = classSnapshot.getKey();
                    classAdapter.add(className);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Set listener for classSpinner
        ClassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedClass = parent.getItemAtPosition(position).toString();
                final ArrayList<String> semesterList = new ArrayList<>();

                // Retrieve semester list for the selected class from Firebase
                FirebaseDatabase.getInstance().getReference("class").child(selectedClass).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                            String semesterName = semesterSnapshot.getKey();
                            semesterList.add(semesterName);
                        }
                        semesterAdapter.clear();
                        semesterAdapter.addAll(semesterList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
                // Display toast message when a class is selected
                Toast.makeText(StudentRegistration.this, "Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set listener for semesterSpinner
        SemesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedSemester = parent.getItemAtPosition(position).toString();
                // Display toast message when a semester is selected
                Toast.makeText(StudentRegistration.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set click listener for Add button using lambda expression
        Add.setOnClickListener(v -> {
            // Get entered values
            String enteredRegdNo = RegdNo.getText().toString();
            String enteredUsername = username.getText().toString();
            String enteredPassword = password.getText().toString();
            String enteredClass = ClassSpinner.getSelectedItem().toString();
            String enteredSemester = SemesterSpinner.getSelectedItem().toString();

            // Validate input fields
            if (enteredRegdNo.isEmpty() || enteredUsername.isEmpty() || enteredPassword.isEmpty() || enteredClass.isEmpty() || enteredSemester.isEmpty()) {
                Toast.makeText(StudentRegistration.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the RegdNo already exists
            databaseReference.child(enteredRegdNo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // RegdNo does not exist, add the student
                        databaseReference.child(enteredRegdNo).child("username").setValue(enteredUsername);
                        databaseReference.child(enteredRegdNo).child("password").setValue(enteredPassword);
                        databaseReference.child(enteredRegdNo).child("class").setValue(enteredClass);
                        databaseReference.child(enteredRegdNo).child("semester").setValue(enteredSemester);
                        Toast.makeText(StudentRegistration.this, "Student added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // RegdNo already exists
                        Toast.makeText(StudentRegistration.this, "User already Exists", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentRegistration.this, "Error adding student", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set click listener for Remove button using lambda expression
        Remove.setOnClickListener(v -> {
            // Get entered RegdNo
            String enteredRegdNo = RegdNo.getText().toString();

            // Validate input field
            if (enteredRegdNo.isEmpty()) {
                Toast.makeText(StudentRegistration.this, "Please enter RegdNo to Remove Student", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the entered RegdNo exists before removing
            databaseReference.child(enteredRegdNo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // RegdNo exists, remove the student
                        databaseReference.child(enteredRegdNo).removeValue();
                        Toast.makeText(StudentRegistration.this, "Student removed Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // RegdNo doesn't exist
                        Toast.makeText(StudentRegistration.this, "Student not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentRegistration.this, "Error removing student", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
