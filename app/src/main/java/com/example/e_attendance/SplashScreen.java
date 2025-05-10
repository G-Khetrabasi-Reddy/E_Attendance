package com.example.e_attendance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Set the content view to the splash screen layout
        // Hide system UI (status bar and navigation bar)
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        // Use a Handler to delay the start of the MainActivity
        new Handler().postDelayed(() -> {
            // Create an Intent to start the MainActivity
            Intent iHome = new Intent(SplashScreen.this, loginActivity.class);
            startActivity(iHome); // Start the MainActivity
            finish(); // Close the SplashActivity to prevent it from being returned to when back button is pressed
        }, 3000); // Delay for 4 seconds (4000 milliseconds)
    }
}
