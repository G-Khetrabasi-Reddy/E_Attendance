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

import java.util.ArrayList;

public class StudentListViewAttendanceByStudent extends AppCompatActivity {
    private LinearLayout studentViewContainerByStudent;
    // Extract data from Intent
    String selectedClass;
    String selectedSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_view_attendance_by_student);

        studentViewContainerByStudent = findViewById(R.id.student_view_container_by_student);

        // Extract data from Intent passed by ViewAttendanceOfStudentsByAdmin and StudentHomePage
        selectedClass = getIntent().getStringExtra("class");
        selectedSemester = getIntent().getStringExtra("semester");
        String selectedStudentId = getIntent().getStringExtra("studentId");

        // ArrayList to store subject IDs
        final ArrayList<String> subjectIdList = new ArrayList<>();

        // Access Firebase Database to retrieve subjects for the selected class and semester
        FirebaseDatabase.getInstance().getReference("class")
                .child(selectedClass)
                .child(selectedSemester) // Assuming subjects are stored under class and semester
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Retrieve subject IDs
                        for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                            String subjectId = subjectSnapshot.getKey();
                            subjectIdList.add(subjectId);
                        }

                        // Calculate attendance for each subject
                        for (String subjectId : subjectIdList) {
                            calculateSubjectAttendance(selectedStudentId, subjectId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    private void calculateSubjectAttendance(final String selectedStudentId, final String subjectId) {
        // Access Firebase Database to get attendance records for the specified subject
        FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                .child(subjectId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Variables to track attendance
                        int totalAttendance = 0;
                        int present = 0;

                        // Iterate over each date in the subject's attendance record
                        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                            // Check if the selected student was present on that date
                            if (dateSnapshot.hasChild(selectedStudentId + ": status") &&
                                    dateSnapshot.child(selectedStudentId + ": status").getValue(String.class).equals("Present")) {
                                present++;
                            }
                            totalAttendance++; // Increment total attendance count
                        }

                        // Calculate percentage
                        double percentage = (present / (double) totalAttendance) * 100;

                        // Display the attendance information
                        displayAttendance(subjectId, totalAttendance, percentage, present);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    private void displayAttendance(String subjectId, int totalAttendance, double percentage, int present) {
        // Access Firebase Database to retrieve the subject name
        FirebaseDatabase.getInstance().getReference("class")
                .child(selectedClass)
                .child(selectedSemester)
                .child(subjectId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Retrieve subject name
                        String subjectName = dataSnapshot.getValue(String.class);
                        if (subjectName != null) {
                            // Inflate the student_card_view_attendance_by_student.xml layout
                            View cardView = LayoutInflater.from(StudentListViewAttendanceByStudent.this).inflate(R.layout.student_card_view_attendance_by_student, studentViewContainerByStudent, false);

                            // Find TextViews in the inflated layout
                            TextView subjectNameTextView = cardView.findViewById(R.id.subject_id_text_view); // Assuming this is for subject name
                            TextView totalAttendanceTextView = cardView.findViewById(R.id.total_attendance_text_view);
                            TextView percentageTextView = cardView.findViewById(R.id.percentage_text_view);

                            // Set serial number
                            TextView serialNumberTextView = cardView.findViewById(R.id.serial_number_text_view);
                            serialNumberTextView.setText(String.format("%02d", studentViewContainerByStudent.getChildCount() + 1));

                            // Set the values for TextViews
                            subjectNameTextView.setText(subjectName);
                            totalAttendanceTextView.setText("Total: " +present+"/"+ totalAttendance);
                            percentageTextView.setText("Percentage: " + String.format("%.2f", percentage) + "%");

                            // Set card view color based on percentage
                            int color = (percentage >= 75) ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
                            cardView.setBackgroundColor(color);

                            // Add the card view to the container
                            studentViewContainerByStudent.addView(cardView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }
}
