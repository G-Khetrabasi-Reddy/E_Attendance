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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class AdminHomePage extends AppCompatActivity {

    // Declare buttons
    Button addRemoveTeacher, addRemoveStudent, subjectRegistration, viewStudentAndSubjectList, changePassword, viewAttendance;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        // Initialize buttons
        addRemoveTeacher = findViewById(R.id.Add_Remove_Teacher);
        addRemoveStudent = findViewById(R.id.Add_Remove_Student);
        subjectRegistration = findViewById(R.id.SubjectRegistration);
        viewStudentAndSubjectList = findViewById(R.id.ViewStudents_Subjects);
        changePassword = findViewById(R.id.change_password_admin);
        viewAttendance = findViewById(R.id.ViewAttendanceAdmin);
        Button logoutButton = findViewById(R.id.logoutAdmin);
        Button batch_manage = findViewById(R.id.BatchManagement);

        // Set click listeners
        addRemoveStudent.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, StudentRegistration.class)));
        addRemoveTeacher.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, TeacherRegistration.class)));
        subjectRegistration.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, SubjectRegistration.class)));

        // Logout action
        logoutButton.setOnClickListener(v -> logout());

        // View student list action
        viewStudentAndSubjectList.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, ViewStudentsAndSubjectsByAdmin.class)));

        // View attendance action
        viewAttendance.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, ViewAttendanceByAdmin.class)));

        // Redirect to BatchManagementByAdmin
        batch_manage.setOnClickListener(v -> startActivity(new Intent(AdminHomePage.this, BatchManagementByAdmin.class)));

        // Change password action
        changePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    // Method to show change password dialog
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminHomePage.this);
        builder.setTitle("Change Password");

        sharedPref = getSharedPreferences("login", MODE_PRIVATE);

        // Inflate dialog layout
        View viewInflated = LayoutInflater.from(AdminHomePage.this).inflate(R.layout.change_password_dialog, null);
        final EditText userIdInput = viewInflated.findViewById(R.id.user_id_input);
        final EditText newPasswordInput = viewInflated.findViewById(R.id.new_password_input);
        final EditText confirmPasswordInput = viewInflated.findViewById(R.id.confirm_password_input);
        builder.setView(viewInflated);

        // Set positive button click listener
        builder.setPositiveButton("Change", (dialog, which) -> {
            String userId = userIdInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(userId)) {
                Toast.makeText(AdminHomePage.this, "Please enter user ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve stored user ID from shared preferences
            String storedUserId = sharedPref.getString("enteredUserid", "");

            // Check if entered user ID matches stored user ID
            if (!userId.equals(storedUserId)) {
                // User IDs don't match
                Toast.makeText(AdminHomePage.this, "User ID doesn't match logged-in user", Toast.LENGTH_SHORT).show();
                userIdInput.setText("");
                newPasswordInput.setText("");
                confirmPasswordInput.setText("");
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(AdminHomePage.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(AdminHomePage.this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(AdminHomePage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                newPasswordInput.setText("");
                confirmPasswordInput.setText("");
                return;
            }

            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(userId);
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        adminRef.child("password").setValue(newPassword);
                        Toast.makeText(AdminHomePage.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminHomePage.this, "User ID not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors
                    Log.w("changePassword", "Error updating password", databaseError.toException());
                }
            });
        });

        // Set negative button click listener
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show dialog
        builder.show();
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loggedIn", false);
        editor.remove("userType");
        editor.apply();

        Intent intent = new Intent(AdminHomePage.this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

