package com.example.e_attendance;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewStudentsAndSubjectsByAdmin extends AppCompatActivity {
    Spinner classSpinner, semesterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students_and_subjects_by_admin);

        // Initialize spinners
        classSpinner = findViewById(R.id.Class);
        semesterSpinner = findViewById(R.id.Semester);

        // Initialize adapters
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        classSpinner.setAdapter(classAdapter);
        semesterSpinner.setAdapter(semesterAdapter);

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
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                Toast.makeText(ViewStudentsAndSubjectsByAdmin.this, "Selected Class: " + selectedClass, LENGTH_SHORT).show();
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
                Toast.makeText(ViewStudentsAndSubjectsByAdmin.this, "Selected Semester: " + selectedSemester, LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set click listener for "View Student's" button
        findViewById(R.id.ViewStudent).setOnClickListener(v -> {
            // Retrieve selected information
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSemester = semesterSpinner.getSelectedItem().toString();

            // Create an Intent to start AvailableStudentListByAdmin activity
            Intent intent = new Intent(ViewStudentsAndSubjectsByAdmin.this, AvailableStudentListByAdmin.class);
            // Pass the selected information to AvailableStudentListByAdmin activity
            intent.putExtra("class", selectedClass);
            intent.putExtra("semester", selectedSemester);

            // Start AvailableStudentListByAdmin activity
            startActivity(intent);
        });

        // Set click listener for "View Subject's" button
        findViewById(R.id.ViewSubject).setOnClickListener(v -> {
            // Retrieve selected information
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSemester = semesterSpinner.getSelectedItem().toString();

            // Create an Intent to start AvailableSubjectListByAdmin activity
            Intent intent = new Intent(ViewStudentsAndSubjectsByAdmin.this, AvailableSubjectListByAdmin.class);
            // Pass the selected information to AvailableSubjectListByAdmin activity
            intent.putExtra("class", selectedClass);
            intent.putExtra("semester", selectedSemester);

            // Start AvailableSubjectListByAdmin activity
            startActivity(intent);
        });
    }
}
