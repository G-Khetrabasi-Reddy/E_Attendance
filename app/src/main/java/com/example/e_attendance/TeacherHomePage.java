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

public class TeacherHomePage extends AppCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home_page);

        // Button for taking attendance
        Button takeAttendanceButton = findViewById(R.id.TakeAttendance);
        takeAttendanceButton.setOnClickListener(v -> {
            // Redirect to TakeAttendance Activity
            Intent intent = new Intent(TeacherHomePage.this, TakeAttendanceByTeacher.class);
            startActivity(intent);
        });

        // Button for viewing attendance
        Button viewAttendanceButton = findViewById(R.id.ViewAttendance);
        viewAttendanceButton.setOnClickListener(v -> {
            // Redirect to ViewAttendance Activity
            Intent intent = new Intent(TeacherHomePage.this, ViewAttendanceByTeacher.class);
            startActivity(intent);
        });

        // Button for changing password
        Button changePassword = findViewById(R.id.change_password_teacher);
        changePassword.setOnClickListener(v -> {
            // Create and show a dialog for password change
            AlertDialog.Builder builder = new AlertDialog.Builder(TeacherHomePage.this);
            builder.setTitle("Change Password");

            sharedPref = getSharedPreferences("login", MODE_PRIVATE);

            // Inflate the layout for the dialog
            View viewInflated = LayoutInflater.from(TeacherHomePage.this).inflate(R.layout.change_password_dialog, null);
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
                String confirmPassword = confirmPasswordInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();

                // Check if user ID is not empty
                if (TextUtils.isEmpty(userId)) {
                    Toast.makeText(TeacherHomePage.this, "Please enter user ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve stored user ID from shared preferences
                String storedUserId = sharedPref.getString("enteredUserid", "");

                // Check if entered user ID matches stored user ID
                if (!userId.equals(storedUserId)) {
                    // User IDs don't match
                    Toast.makeText(TeacherHomePage.this, "User ID doesn't match logged-in user", Toast.LENGTH_SHORT).show();
                    userIdInput.setText("");
                    newPasswordInput.setText("");
                    confirmPasswordInput.setText("");
                    return;
                }

                // Check if new password is not empty
                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(TeacherHomePage.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if confirm password is not empty
                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(TeacherHomePage.this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(TeacherHomePage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    newPasswordInput.setText("");
                    confirmPasswordInput.setText("");
                    return;
                }

                // Query the database for the user ID
                DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference().child("Teacher").child(userId);
                teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                                teacherRef.child("password").setValue(confirmPassword);
                                Toast.makeText(TeacherHomePage.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TeacherHomePage.this, "User ID not found", Toast.LENGTH_SHORT).show();
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
        });

        // Button for logging out
        Button logoutButton = findViewById(R.id.logoutTeacher);
        logoutButton.setOnClickListener(v -> logout());
    }
    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loggedIn", false);
        editor.remove("userType");
        editor.apply();

        Intent intent = new Intent(TeacherHomePage.this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
