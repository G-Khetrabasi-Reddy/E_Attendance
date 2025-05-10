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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentListViewAttendanceBySubject extends AppCompatActivity {

    private LinearLayout studentViewContainerBySubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_view_attendance_by_subject);

        // Initialize LinearLayout for student card views
        studentViewContainerBySubject = findViewById(R.id.student_view_container_by_subject);

        // Retrieve selected values passed from ViewAttendanceByTeacher activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Extract selected class, semester, and subject ID
            String selectedClass = extras.getString("class_name");
            String selectedSemester = extras.getString("semester");
            String selectedSubjectId = extras.getString("subject_id");

            // Query Firebase to retrieve student IDs where class and semester match selected values
            Query query = FirebaseDatabase.getInstance().getReference("Student")
                    .orderByChild("class").equalTo(selectedClass);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // List to store student IDs
                    ArrayList<String> studentIds = new ArrayList<>();
                    // Iterate through student data
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        String semester = studentSnapshot.child("semester").getValue(String.class);
                        // Add student ID if semester matches selected semester
                        if (semester != null && semester.equals(selectedSemester)) {
                            studentIds.add(studentId);
                        }
                    }
                    // Calculate attendance for each student
                    calculateAttendance(selectedSubjectId, studentIds);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }

    // Method to calculate attendance for each student
    private void calculateAttendance(String selectedSubjectId, ArrayList<String> studentIds) {
        // Query Firebase to retrieve total attendance and individual student attendance
        FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                .child(selectedSubjectId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Variables to store total attendance and student-wise attendance
                        int totalAttendance = (int) dataSnapshot.getChildrenCount();
                        HashMap<String, Integer> studentAttendanceMap = new HashMap<>();

                        // Iterate through attendance records
                        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                                String studentId = studentSnapshot.getKey().split(":")[0]; // Extract student ID from key
                                String status = studentSnapshot.getValue(String.class);

                                // Increment student's attendance count if present
                                if (status != null && status.equals("Present")) {
                                    studentAttendanceMap.put(studentId, studentAttendanceMap.getOrDefault(studentId, 0) + 1);
                                }
                            }
                        }

                        // Add student card views for each student
                        for (String studentId : studentIds) {
                            addStudentCardView(studentId, studentAttendanceMap, totalAttendance);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    // Method to add a card view for each student showing attendance details
    private void addStudentCardView(String studentId, HashMap<String, Integer> studentAttendanceMap, int totalAttendance) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.student_card_view_attendance_by_subject, studentViewContainerBySubject, false);

        // Find TextViews in the card view layout
        TextView serialNumberTextView = cardView.findViewById(R.id.serial_number_text_view);
        TextView studentIdTextView = cardView.findViewById(R.id.student_id_text_view);
        TextView totalAttendanceTextView = cardView.findViewById(R.id.total_attendance_text_view);
        TextView percentageTextView = cardView.findViewById(R.id.percentage_text_view);

        // Set serial number
        serialNumberTextView.setText(String.format("%02d", studentViewContainerBySubject.getChildCount() + 1));

        // Set student ID
        studentIdTextView.setText(studentId);

        // Calculate and set total attendance for the student
        int studentAttendance = studentAttendanceMap.containsKey(studentId) ? studentAttendanceMap.get(studentId) : 0;
        totalAttendanceTextView.setText("Total: " + studentAttendance + "/" + totalAttendance);

        // Calculate and set attendance percentage for the student
        double percentage = ((double) studentAttendance / totalAttendance) * 100;
        String formattedPercentage = String.format("%.2f", percentage); // Format percentage with 2 decimal places
        percentageTextView.setText("Percentage: " + formattedPercentage + "%");

        // Set card view background color based on attendance percentage
        int color = percentage > 75 ? R.color.green : R.color.red;
        cardView.findViewById(R.id.card_view_background).setBackgroundColor(getResources().getColor(color));

        // Add the card view to the container
        studentViewContainerBySubject.addView(cardView);
    }
}
