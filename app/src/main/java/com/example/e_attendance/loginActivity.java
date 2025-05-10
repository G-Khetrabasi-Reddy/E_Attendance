package com.example.e_attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    Button loginButton;
    EditText userid, password;
    Spinner spinner;
    String user_role;
    final private String[] userRoleString = new String[]{"Admin", "Teacher", "Student"};
    private SharedPreferences sharedPref;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences for user login state
        sharedPref = getSharedPreferences("login", Context.MODE_PRIVATE);

        // Check for previously logged-in user
        if (sharedPref.getBoolean("loggedIn", false)) {
            String userType = sharedPref.getString("userType", "");
            String enteredUserid = sharedPref.getString("enteredUserid", "");

            // Redirect to home page with both userType and enteredUserid
            redirectToHomePage(userType, enteredUserid);
        }


        // Initialize UI components
        loginButton = findViewById(R.id.loginButton);
        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);
        spinner = findViewById(R.id.spinner);

        // Set up spinner for selecting user role
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ((TextView) arg0.getChildAt(0)).setTextColor(Color.BLACK);
                user_role = (String) spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Do nothing
            }
        });

        ArrayAdapter<String> adapter_role = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, userRoleString);
        adapter_role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter_role);

        // Set login button click listener
        loginButton.setOnClickListener(v -> {
            // Check internet connection
            if (!isConnected()) {
                Toast.makeText(loginActivity.this, "No internet connection. Please check your network settings.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedUsertype = spinner.getSelectedItem().toString();
            String enteredUserid = userid.getText().toString();
            String enteredPassword = password.getText().toString();

            // Validate user inputs
            if (enteredUserid.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(loginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine database reference based on user role
            switch (selectedUsertype) {
                case "Admin":
                    reference = FirebaseDatabase.getInstance().getReference("Admin");
                    break;
                case "Teacher":
                    reference = FirebaseDatabase.getInstance().getReference("Teacher");
                    break;
                case "Student":
                    reference = FirebaseDatabase.getInstance().getReference("Student");
                    break;
                default:
                    Toast.makeText(loginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                    return;
            }

            // Authenticate user
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(enteredUserid)) {
                        String storedPassword = snapshot.child(enteredUserid).child("password").getValue(String.class);
                        if (enteredPassword.equals(storedPassword)) {
                            // Mark user as logged in and save user ID in shared preferences
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("loggedIn", true);
                            editor.putString("userType", selectedUsertype);
                            editor.putString("enteredUserid", enteredUserid); // Save entered user ID
                            editor.apply();

                            // Redirect to home page with userid
                            redirectToHomePage(selectedUsertype, enteredUserid);
                        } else {
                            Toast.makeText(loginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(loginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(loginActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Check internet connection
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // Redirect to home page based on user type, including userid
    private void redirectToHomePage(String userType, String enteredUserid) {
        Intent intent = null;
        switch (userType) {
            case "Admin":
                intent = new Intent(loginActivity.this, AdminHomePage.class);
                break;
            case "Teacher":
                intent = new Intent(loginActivity.this, TeacherHomePage.class);
                break;
            case "Student":
                intent = new Intent(loginActivity.this, StudentHomePage.class);
                break;
            default:
                intent = new Intent(loginActivity.this, loginActivity.class);
                break;
        }

        if (intent != null) { // Check if intent was created successfully
            intent.putExtra("userid", enteredUserid); // Add userid to the intent
            startActivity(intent);
            finish();
        } else {
            // Handle error if intent creation failed
            Toast.makeText(loginActivity.this, "Error creating intent", Toast.LENGTH_SHORT).show();
        }
    }

}
