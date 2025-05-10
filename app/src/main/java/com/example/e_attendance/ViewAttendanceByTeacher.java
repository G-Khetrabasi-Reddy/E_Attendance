package com.example.e_attendance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewAttendanceByTeacher extends AppCompatActivity {

    Spinner classSpinner, semesterSpinner, subjectSpinner;
    EditText showDate;
    ImageView dateIcon;
    Button viewButton;

    Map<String, String> subjectIdMap; // Map to store subject name and corresponding ID
    ArrayAdapter<String> subjectAdapter; // Adapter for the subjectSpinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_by_teacher);

        // Initialize views
        classSpinner = findViewById(R.id.Class);
        semesterSpinner = findViewById(R.id.Semester);
        subjectSpinner = findViewById(R.id.Subject);
        showDate = findViewById(R.id.showdate);
        dateIcon = findViewById(R.id.dateIcon);
        viewButton = findViewById(R.id.View);

        // Initialize adapters
        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        classSpinner.setAdapter(classAdapter);
        semesterSpinner.setAdapter(semesterAdapter);
        subjectSpinner.setAdapter(subjectAdapter);

        // Initialize subjectIdMap
        subjectIdMap = new HashMap<>();

        // Set click listener for View button
        viewButton.setOnClickListener(v -> {
            String selectedSubject = subjectSpinner.getSelectedItem().toString();
            String subjectId = subjectIdMap.get(selectedSubject);
            String selectedDate = showDate.getText().toString().trim();

            // Check if the date is not empty
            if (!selectedDate.isEmpty()) {
                // Redirect to StudentListViewAttendanceByDate activity with subject ID and date
                Intent intent = new Intent(ViewAttendanceByTeacher.this, StudentListViewAttendanceByDate.class);
                intent.putExtra("subject_id", subjectId);
                intent.putExtra("date", selectedDate);
                startActivity(intent);
            } else {
                String selectedClass = classSpinner.getSelectedItem().toString();
                String selectedSemester = semesterSpinner.getSelectedItem().toString();

                if (selectedClass.isEmpty() || selectedSemester.isEmpty() || selectedSubject.isEmpty()) {
                    Toast.makeText(ViewAttendanceByTeacher.this, "Please select all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Redirect to StudentListViewAttendanceBySubject activity with data
                Intent intent = new Intent(ViewAttendanceByTeacher.this, StudentListViewAttendanceBySubject.class);
                intent.putExtra("class_name", selectedClass);
                intent.putExtra("semester", selectedSemester);
                intent.putExtra("subject_id", subjectId);
                startActivity(intent);
            }
        });

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
                Toast.makeText(ViewAttendanceByTeacher.this, "Error retrieving classes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set listener for classSpinner
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedClass = parent.getItemAtPosition(position).toString();
                semesterAdapter.clear(); // Clear previous semester data

                // Retrieve semester list for the selected class from Firebase
                FirebaseDatabase.getInstance().getReference("class").child(selectedClass).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> semesterList = new ArrayList<>();
                        for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                            String semesterName = semesterSnapshot.getKey();
                            semesterList.add(semesterName);
                        }
                        semesterAdapter.addAll(semesterList);

                        // Reset subject selection and update subjectAdapter based on default semester (assuming first semester)
                        subjectSpinner.setSelection(0);
                        updateSubjectAdapter(selectedClass, semesterList.get(0));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });

                // Display toast message when a class is selected
                Toast.makeText(ViewAttendanceByTeacher.this, "Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
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
                final String selectedClass = classSpinner.getSelectedItem().toString();
                final String selectedSemester = parent.getItemAtPosition(position).toString();

                // Update subjectAdapter based on the selected class and semester
                updateSubjectAdapter(selectedClass, selectedSemester);

                // Display toast message when a semester is selected (optional)
                Toast.makeText(ViewAttendanceByTeacher.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set click listener for date icon ImageView
        dateIcon.setOnClickListener(v -> {
            // Get current date to set as default in the date picker dialog
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(ViewAttendanceByTeacher.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update the EditText with the selected date
                        showDate.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, (monthOfYear + 1), year1));
                    }, year, month, day);
            // Show the date picker dialog
            datePickerDialog.show();
        });
    }

    // Function to update subjectAdapter and subjectIdMap based on selected class and semester
    private void updateSubjectAdapter(String selectedClass, String selectedSemester) {
        final ArrayList<String> subjectList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("class").child(selectedClass).child(selectedSemester).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectAdapter.clear();
                subjectIdMap.clear();
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subjectId = subjectSnapshot.getKey();
                    String subjectName = subjectSnapshot.getValue(String.class); // Assuming subject name is stored as value
                    subjectList.add(subjectName);
                    subjectIdMap.put(subjectName, subjectId); // Store subject name and corresponding ID
                }
                subjectAdapter.addAll(subjectList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
