package com.example.e_attendance;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SemesterUpdateOfStudentByAdmin extends AppCompatActivity {

    private Spinner classSpinner, oldsemesterSpinner, newsemesterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_update_of_student_by_admin);

        // Initialize spinners
        classSpinner = findViewById(R.id.Class);
        oldsemesterSpinner = findViewById(R.id.OldSemester);
        newsemesterSpinner = findViewById(R.id.ChangeSemester);

        // Initialize adapters
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> old_semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> new_semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        classSpinner.setAdapter(classAdapter);
        oldsemesterSpinner.setAdapter(old_semesterAdapter);
        newsemesterSpinner.setAdapter(new_semesterAdapter);

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
                        old_semesterAdapter.clear();
                        old_semesterAdapter.addAll(semesterList);
                        new_semesterAdapter.clear();
                        new_semesterAdapter.addAll(semesterList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
                // Display toast message when a class is selected
                Toast.makeText(SemesterUpdateOfStudentByAdmin.this, "Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set listener for old semesterSpinner
        oldsemesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedSemester = parent.getItemAtPosition(position).toString();

                // Display toast message when a semester is selected
                Toast.makeText(SemesterUpdateOfStudentByAdmin.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set listener for new semesterSpinner
        newsemesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedSemester = parent.getItemAtPosition(position).toString();

                // Display toast message when a semester is selected
                Toast.makeText(SemesterUpdateOfStudentByAdmin.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        findViewById(R.id.Change).setOnClickListener(v -> {
            // Retrieve selected information
            String selectedClass = classSpinner.getSelectedItem().toString();
            String oldSelectedSemester = oldsemesterSpinner.getSelectedItem().toString();
            String newSelectedSemester = newsemesterSpinner.getSelectedItem().toString();

            // Update students' semesters
            updateStudentsSemester(selectedClass, oldSelectedSemester, newSelectedSemester);
        });
    }

    private void updateStudentsSemester(String selectedClass, String oldSelectedSemester, String newSelectedSemester) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference studentRef = database.getReference("Student");

        studentRef.orderByChild("class").equalTo(selectedClass)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                            String studentId = studentSnapshot.getKey();
                            String currentSemester = studentSnapshot.child("semester").getValue(String.class);

                            if (currentSemester.equals(oldSelectedSemester)) {
                                // Update semester to newSelectedSemester
                                studentSnapshot.getRef().child("semester").setValue(newSelectedSemester);
                                Toast.makeText(SemesterUpdateOfStudentByAdmin.this,
                                        "Student " + studentId + " semester updated successfully!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                        Toast.makeText(SemesterUpdateOfStudentByAdmin.this, "Error updating semesters: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
