package com.example.e_attendance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeleteAllStudentsInSemesterByAdmin extends AppCompatActivity {
    Spinner classSpinner, semesterSpinner;
    Button deleteStudentsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_all_students_in_semester_by_admin);

        // Initialize spinners and button
        classSpinner = findViewById(R.id.Class);
        semesterSpinner = findViewById(R.id.Semester);
        deleteStudentsButton = findViewById(R.id.DeleteStudent);

        // Initialize adapters
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        classSpinner.setAdapter(classAdapter);
        semesterSpinner.setAdapter(semesterAdapter);

        // Retrieve class list from Firebase
        FirebaseDatabase.getInstance().getReference("class").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    String className = classSnapshot.getKey();
                    classAdapter.add(className);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error retrieving classes: " + databaseError.getMessage());
            }
        });

        // Set listener for classSpinner
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedClass = parent.getItemAtPosition(position).toString();
                final ArrayList<String> semesterList = new ArrayList<>();

                // Retrieve semester list for the selected class from Firebase
                FirebaseDatabase.getInstance().getReference("class").child(selectedClass).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                            String semesterName = semesterSnapshot.getKey();
                            semesterList.add(semesterName);
                        }
                        semesterAdapter.clear();
                        semesterAdapter.addAll(semesterList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle errors
                        Log.e("Firebase", "Error retrieving semesters: " + databaseError.getMessage());
                    }
                });
                // Display toast message when a class is selected
                Toast.makeText(DeleteAllStudentsInSemesterByAdmin.this, "Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set listener for semesterSpinner
        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedSemester = parent.getItemAtPosition(position).toString();

                // Display toast message when a semester is selected
                Toast.makeText(DeleteAllStudentsInSemesterByAdmin.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set click listener for "Delete Students" button
        deleteStudentsButton.setOnClickListener(v -> {
            // Retrieve selected information
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSemester = semesterSpinner.getSelectedItem().toString();

            // Confirm deletion with user
            confirmAndDeleteStudents(selectedClass, selectedSemester);
        });
    }

    // Method to confirm and delete students
    private void confirmAndDeleteStudents(final String selectedClass, final String selectedSemester) {
        // Build the message including the number of students to be deleted
        final DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("Student");
        studentRef.orderByChild("class").equalTo(selectedClass)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int studentCount = 0;
                        for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                            String studentClass = studentSnapshot.child("class").getValue(String.class);
                            String studentSemester = studentSnapshot.child("semester").getValue(String.class);

                            if (selectedClass.equals(studentClass) && selectedSemester.equals(studentSemester)) {
                                studentCount++;
                            }
                        }

                        // Build and show the AlertDialog
                        String message = "This will delete " + studentCount + " students. Are you sure?";
                        new AlertDialog.Builder(DeleteAllStudentsInSemesterByAdmin.this)
                                .setTitle("Confirm Deletion")
                                .setMessage(message)
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                    // User confirmed deletion, proceed with deleting students
                                    deleteStudents(selectedClass, selectedSemester);
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                        Log.e("Firebase", "confirmAndDeleteStudents:onCancelled", databaseError.toException());
                    }
                });
    }

    // Method to delete students
    private void deleteStudents(final String selectedClass, final String selectedSemester) {
        final DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("Student");

        studentRef.orderByChild("class").equalTo(selectedClass)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                            String studentClass = studentSnapshot.child("class").getValue(String.class);
                            String studentSemester = studentSnapshot.child("semester").getValue(String.class);

                            if (selectedClass.equals(studentClass) && selectedSemester.equals(studentSemester)) {
                                // Delete this student
                                studentSnapshot.getRef().removeValue();
                            }
                        }
                        Toast.makeText(DeleteAllStudentsInSemesterByAdmin.this, "Students deleted successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                        Log.e("Firebase", "deleteStudents:onCancelled", databaseError.toException());
                    }
                });
    }
}
