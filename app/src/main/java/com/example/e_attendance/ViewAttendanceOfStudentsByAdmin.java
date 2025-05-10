package com.example.e_attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAttendanceOfStudentsByAdmin extends AppCompatActivity {

    private Spinner classSpinner, semesterSpinner, studentIdSpinner;
    private Button viewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_of_students_by_admin);

        initializeViews();
        setupClassSpinner();
        setupViewButton();
    }

    private void initializeViews() {
        classSpinner = findViewById(R.id.Class);
        semesterSpinner = findViewById(R.id.Semester);
        studentIdSpinner = findViewById(R.id.StudentId);
        viewButton = findViewById(R.id.ViewBtn);
    }

    private void setupClassSpinner() {
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

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

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedClass = parent.getItemAtPosition(position).toString();
                clearSpinners();
                populateSemesterSpinner(selectedClass);
                showToast("Selected Class: " + selectedClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void clearSpinners() {
        semesterSpinner.setAdapter(null);
        studentIdSpinner.setAdapter(null);
    }

    private void populateSemesterSpinner(final String selectedClass) {
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(semesterAdapter);

        FirebaseDatabase.getInstance().getReference("class").child(selectedClass).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> semesterList = new ArrayList<>();
                for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                    String semesterName = semesterSnapshot.getKey();
                    semesterList.add(semesterName);
                }
                semesterAdapter.addAll(semesterList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedSemester = parent.getItemAtPosition(position).toString();
                populateStudentIdSpinner(selectedClass, selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void populateStudentIdSpinner(final String selectedClass, final String selectedSemester) {
        final ArrayAdapter<String> studentIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        studentIdSpinner.setAdapter(studentIdAdapter);

        Query query = FirebaseDatabase.getInstance().getReference("Student")
                .orderByChild("class").equalTo(selectedClass);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> studentIds = new ArrayList<>();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    String semester = studentSnapshot.child("semester").getValue(String.class);
                    if (semester != null && semester.equals(selectedSemester)) {
                        studentIds.add(studentId);
                    }
                }
                studentIdAdapter.addAll(studentIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void setupViewButton() {
        viewButton.setOnClickListener(v -> {
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSemester = semesterSpinner.getSelectedItem().toString();
            String selectedStudentId = studentIdSpinner.getSelectedItem().toString();
            startAttendanceActivity(selectedClass, selectedSemester, selectedStudentId);
        });
    }

    private void startAttendanceActivity(String selectedClass, String selectedSemester, String selectedStudentId) {
        Intent intent = new Intent(ViewAttendanceOfStudentsByAdmin.this, StudentListViewAttendanceByStudent.class);
        intent.putExtra("class", selectedClass);
        intent.putExtra("semester", selectedSemester);
        intent.putExtra("studentId", selectedStudentId);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
