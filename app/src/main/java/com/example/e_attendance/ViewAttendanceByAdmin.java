package com.example.e_attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewAttendanceByAdmin extends AppCompatActivity {

    Button ViewAttendanceSubject, ViewAttendanceStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_by_admin);

        // Initialize buttons
        ViewAttendanceSubject = findViewById(R.id.ViewAttendanceSubject);
        ViewAttendanceStudent = findViewById(R.id.ViewAttendanceStudent);

        // Set click listener for ViewAttendanceSubject button
        ViewAttendanceSubject.setOnClickListener(v -> {
            // Redirect to ViewAttendanceByTeacher activity
            Intent intent = new Intent(ViewAttendanceByAdmin.this, ViewAttendanceByTeacher.class);
            startActivity(intent);
        });

        // Set click listener for ViewAttendanceStudent button
        ViewAttendanceStudent.setOnClickListener(v -> {
            // Redirect to ViewAttendanceOfStudentsByAdmin activity
            Intent intent = new Intent(ViewAttendanceByAdmin.this, ViewAttendanceOfStudentsByAdmin.class);
            startActivity(intent);
        });
    }
}
