package com.example.e_attendance;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StudentListViewTakeAttendance extends AppCompatActivity {

    private LinearLayout studentContainer;
    private TextView presentTextView, absentTextView;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_view_take_attendance);

        // Initialize views
        studentContainer = findViewById(R.id.student_container1);
        presentTextView = findViewById(R.id.presentTextView);
        absentTextView = findViewById(R.id.absentTextView);
        save = findViewById(R.id.saveButton);

        // Retrieve selected values passed from TakeAttendance activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String selectedClass = extras.getString("class");
            String selectedSemester = extras.getString("semester");

            // Initialize ArrayList to store student IDs
            final ArrayList<String> studentIds = new ArrayList<>();

            // Query Firebase to retrieve student IDs where class and semester match selected values
            Query query = FirebaseDatabase.getInstance().getReference("Student")
                    .orderByChild("class").equalTo(selectedClass);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        String semester = studentSnapshot.child("semester").getValue(String.class);
                        if (semester.equals(selectedSemester)) {
                            studentIds.add(studentId);
                        }
                    }

                    // Add card views for each student
                    for (String studentId : studentIds) {
                        addStudentCardView(studentId);
                    }
                    updateAttendanceCount(); // Initially update the counts
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }

        // Set click listener for saveButton using lambda expression
        save.setOnClickListener(v -> {
            // Build an AlertDialog for confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(StudentListViewTakeAttendance.this);
            builder.setTitle("Confirm Save");
            builder.setMessage("Are you sure you want to save the attendance ?");
            builder.setPositiveButton("Yes", (dialog, which) -> saveAttendance());
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    // Method to save attendance to Firebase
    private void saveAttendance() {
        String selectedSubjectId = getIntent().getStringExtra("subject");
        String selectedDate = getIntent().getStringExtra("date");

        // Reference to the AttendanceRecord node for the selected subject
        FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                .child(selectedSubjectId)
                .child(selectedDate) // Format the date
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Iterate over the child views to get student IDs and attendance status
                        for (int i = 0; i < studentContainer.getChildCount(); i++) {
                            View childView = studentContainer.getChildAt(i);
                            TextView studentIdTextView = childView.findViewById(R.id.student_name_text_view);
                            String studentId = studentIdTextView.getText().toString();

                            CheckBox attendanceCheckBox = childView.findViewById(R.id.attendance_checkbox);
                            boolean isPresent = attendanceCheckBox.isChecked();

                            // Store the attendance record in Firebase
                            FirebaseDatabase.getInstance().getReference("AttendanceRecord")
                                    .child(selectedSubjectId)
                                    .child(selectedDate) // Format the date
                                    .child(studentId + ": status") // Append student_id: status
                                    .setValue(isPresent ? "Present" : "Absent");
                        }

                        // Display success message
                        Toast.makeText(StudentListViewTakeAttendance.this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    // Method to add a card view for each student
    private void addStudentCardView(String studentId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.student_card_view_take_attendance, null);

        // Find views within the card view layout
        TextView serialNumberTextView = cardView.findViewById(R.id.serial_number_text_view);
        TextView studentIdTextView = cardView.findViewById(R.id.student_name_text_view);
        final CheckBox attendanceCheckBox = cardView.findViewById(R.id.attendance_checkbox);

        // Set serial number
        serialNumberTextView.setText(String.format("%02d", studentContainer.getChildCount() + 1));

        // Set student ID
        studentIdTextView.setText(studentId);

        // Set click listeners for card view and checkbox
        cardView.setOnClickListener(v -> {
            attendanceCheckBox.toggle();
            updateAttendanceCount(); // Update attendance count when card view is clicked
        });
        attendanceCheckBox.setOnClickListener(v -> updateAttendanceCount()); // Update attendance count when checkbox is clicked

        // Add margin to card view
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(layoutParams);

        // Add the card view to the container
        studentContainer.addView(cardView);
    }

    // Method to update present and absent counts
    private void updateAttendanceCount() {
        int presentCount = 0, absentCount = 0;
        for (int i = 0; i < studentContainer.getChildCount(); i++) {
            View childView = studentContainer.getChildAt(i);
            CheckBox attendanceCheckBox = childView.findViewById(R.id.attendance_checkbox);
            if (attendanceCheckBox.isChecked()) {
                presentCount++;
            } else {
                absentCount++;
            }
        }
        // Update present and absent counts in TextViews
        presentTextView.setText(getString(R.string.present_count, presentCount));
        absentTextView.setText(getString(R.string.absent_count, absentCount));
    }
}
