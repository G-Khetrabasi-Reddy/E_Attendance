package com.example.e_attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentHomePage extends AppCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home_page);

        // Initialize buttons
        Button viewButton = findViewById(R.id.ViewAttendance);
        Button changePassword = findViewById(R.id.change_password_student);
        Button checkAttendance = findViewById(R.id.CheckAttendance);
        Button logoutButton = findViewById(R.id.logoutStudent);

        // Retrieve shared preferences
        sharedPref = getSharedPreferences("login", MODE_PRIVATE);

        // Set onClickListener using lambda expression
        viewButton.setOnClickListener(v -> startAttendanceActivity());

        // Set onClickListener using lambda expression
        checkAttendance.setOnClickListener(v -> {
            // Redirect to ViewAttendanceOfStudentsByAdmin Activity
            startActivity(new Intent(StudentHomePage.this, CheckAttendanceOfDateByStudent.class));
        });

        // Set onClickListener using lambda expression
        changePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Set onClickListener using lambda expression
        logoutButton.setOnClickListener(v -> logout());
    }

    // Method to show the change password dialog
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentHomePage.this);
        builder.setTitle("Change Password");

        // Inflate the layout for the dialog
        View viewInflated = LayoutInflater.from(StudentHomePage.this).inflate(R.layout.change_password_dialog, null);
        // Set up the input
        final EditText userIdInput = viewInflated.findViewById(R.id.user_id_input);
        final EditText newPasswordInput = viewInflated.findViewById(R.id.new_password_input);
        final EditText confirmPasswordInput = viewInflated.findViewById(R.id.confirm_password_input);

        // Set the layout for the dialog
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Change", (dialog, which) -> {
            // Retrieve user inputs
            String userId = userIdInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Check if user ID is not empty
            if (TextUtils.isEmpty(userId)) {
                Toast.makeText(StudentHomePage.this, "Please enter user ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve stored user ID from shared preferences
            String storedUserId = sharedPref.getString("enteredUserid", "");

            // Check if entered user ID matches stored user ID
            if (!userId.equals(storedUserId)) {
                // User IDs don't match
                Toast.makeText(StudentHomePage.this, "User ID doesn't match logged-in user", Toast.LENGTH_SHORT).show();
                userIdInput.setText("");
                newPasswordInput.setText("");
                confirmPasswordInput.setText("");
                return;
            }

            // Check if new password is not empty
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(StudentHomePage.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if confirm password is not empty
            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(StudentHomePage.this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(StudentHomePage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                newPasswordInput.setText("");
                confirmPasswordInput.setText("");
                return;
            }

            // Query the database for the user ID
            DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference().child("Student").child(userId);
            studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        studentRef.child("password").setValue(confirmPassword);
                        Toast.makeText(StudentHomePage.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StudentHomePage.this, "User ID not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors
                    Log.w("changePassword", "Error updating password", databaseError.toException());
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loggedIn", false);
        editor.remove("userType");
        editor.apply();

        Intent intent = new Intent(StudentHomePage.this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void startAttendanceActivity() {
        // Retrieve stored user ID from shared preferences
        String storedUserId = sharedPref.getString("enteredUserid", "");

        // Get reference to the database
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("Student").child(storedUserId);

        // Attach a ValueEventListener to fetch data
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve class and semester values from dataSnapshot
                    String studentClass = dataSnapshot.child("class").getValue(String.class);
                    String semester = dataSnapshot.child("semester").getValue(String.class);

                    // Create an intent to start StudentListViewAttendanceByStudent activity
                    Intent intent = new Intent(StudentHomePage.this, StudentListViewAttendanceByStudent.class);

                    // Pass class, semester, and storedUserId to the next activity
                    intent.putExtra("class", studentClass);
                    intent.putExtra("semester", semester);
                    intent.putExtra("studentId", storedUserId);

                    // Start the activity
                    startActivity(intent);
                } else {
                    // User ID not found in the database
                    Toast.makeText(StudentHomePage.this, "User ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
                Log.e("StudentHomePage", "Error fetching user data", databaseError.toException());
                Toast.makeText(StudentHomePage.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
