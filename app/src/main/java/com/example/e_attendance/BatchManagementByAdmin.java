package com.example.e_attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class BatchManagementByAdmin extends AppCompatActivity {

    Button shift_batch, delete_batch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_management_by_admin);

        // Initialize buttons
        shift_batch = findViewById(R.id.shiftbatch);
        delete_batch = findViewById(R.id.deletebatch);

        // Set click listener for the "Shift Batch" button
        shift_batch.setOnClickListener(v -> {
            // Start SemesterUpdateOfStudentByAdmin activity
            Intent intent = new Intent(BatchManagementByAdmin.this, SemesterUpdateOfStudentByAdmin.class);
            startActivity(intent);
        });

        // Set click listener for the "Delete Batch" button
        delete_batch.setOnClickListener(v -> {
            // Start DeleteAllStudentsInSemesterByAdmin activity
            Intent intent = new Intent(BatchManagementByAdmin.this, DeleteAllStudentsInSemesterByAdmin.class);
            startActivity(intent);
        });
    }
}
