package com.example.e_attendance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TakeAttendanceByTeacher extends AppCompatActivity {

    Spinner classSpinner, semesterSpinner, subjectSpinner;
    EditText showDate;
    ImageView dateIcon;

    Map<String, String> subjectIdMap; // Map to store subject name and corresponding ID
    ArrayAdapter<String> subjectAdapter; // Adapter for the subjectSpinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance_by_teacher);

        classSpinner = findViewById(R.id.Class);
        semesterSpinner = findViewById(R.id.Semester);
        subjectSpinner = findViewById(R.id.Subject);
        showDate = findViewById(R.id.showdate);
        dateIcon = findViewById(R.id.dateIcon);

        final ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        final ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        classSpinner.setAdapter(classAdapter);
        semesterSpinner.setAdapter(semesterAdapter);
        subjectSpinner.setAdapter(subjectAdapter);

        subjectIdMap = new HashMap<>();

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
                Log.e("TakeAttendance", "Error retrieving classes: " + databaseError.getMessage());
                Toast.makeText(TakeAttendanceByTeacher.this, "Error retrieving classes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.e("TakeAttendance", "Error retrieving semesters: " + databaseError.getMessage());
                        // Handle errors
                    }
                });

                // Display toast message when a class is selected
                Toast.makeText(TakeAttendanceByTeacher.this, "Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(TakeAttendanceByTeacher.this, "Selected Semester: " + selectedSemester, Toast.LENGTH_SHORT).show();
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(TakeAttendanceByTeacher.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update the EditText with the selected date
                        showDate.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, (monthOfYear + 1), year1));
                    }, year, month, day);
            // Show the date picker dialog
            datePickerDialog.show();
        });

        findViewById(R.id.Take).setOnClickListener(v -> {
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSemester = semesterSpinner.getSelectedItem().toString();
            String selectedSubject = subjectSpinner.getSelectedItem().toString();
            String selectedSubjectId = subjectIdMap.get(selectedSubject);
            String selectedDate = showDate.getText().toString();

            if (selectedDate == null || selectedSubjectId == null) {
                Log.e("TakeAttendance", "Date or selectedSubjectId is null");
                return;
            }

            SimpleDateFormat sdfInput = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date;
            try {
                date = sdfInput.parse(selectedDate);
            } catch (ParseException e) {
                Log.e("TakeAttendance", "Error parsing date: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = sdfOutput.format(date);

            Intent intent = new Intent(TakeAttendanceByTeacher.this, StudentListViewTakeAttendance.class);
            intent.putExtra("class", selectedClass);
            intent.putExtra("semester", selectedSemester);
            intent.putExtra("subject", selectedSubjectId);
            intent.putExtra("date", formattedDate);

            startActivity(intent);
        });

        findViewById(R.id.Delete).setOnClickListener(v -> {
            final String selectedSubject = subjectSpinner.getSelectedItem().toString();
            final String selectedSubjectId = subjectIdMap.get(selectedSubject);
            final String selectedDate = showDate.getText().toString();

            // Show a dialog box for confirmation
            if (selectedDate.isEmpty()) {
                // If the selected date is empty, prompt to delete entire subject's attendance record
                new AlertDialog.Builder(TakeAttendanceByTeacher.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete the entire "+selectedSubject+"'s attendance record?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // User confirmed to delete entire subject's attendance record
                            deleteSubjectAttendance(selectedSubjectId);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            } else {
                // If a specific date is selected, prompt to delete attendance record for that date
                new AlertDialog.Builder(TakeAttendanceByTeacher.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete attendance record of "+selectedSubject+" for " + selectedDate + "?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // User confirmed to delete attendance record for the selected date
                            deleteAttendanceRecord(selectedSubjectId, selectedDate);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
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
                Log.e("TakeAttendance", "Error retrieving subjects: " + databaseError.getMessage());
                // Handle errors
            }
        });
    }

    private void deleteSubjectAttendance(String selectedSubjectId) {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("AttendanceRecord").child(selectedSubjectId);
        attendanceRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TakeAttendanceByTeacher.this, "Attendance record for the subject deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("TakeAttendance", "Failed to delete attendance record: " + e.getMessage());
                    Toast.makeText(TakeAttendanceByTeacher.this, "Failed to delete attendance record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAttendanceRecord(String selectedSubjectId, String selectedDate) {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                .child(selectedSubjectId).child(selectedDate);
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the data exists, delete the attendance record for the selected date
                    attendanceRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(TakeAttendanceByTeacher.this, "Attendance record for the selected date deleted successfully!", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // If the data doesn't exist, show an error message
                    Toast.makeText(TakeAttendanceByTeacher.this, "No attendance record found for the selected date", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TakeAttendance", "Error: " + databaseError.getMessage());
                Toast.makeText(TakeAttendanceByTeacher.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
