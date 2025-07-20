package com.denzo.mypomodoro;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set version number dynamically
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView versionTextView = findViewById(R.id.versionTextView);
            versionTextView.setText(String.format("Version %s", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Set up button click listeners
        Button contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> contactDeveloper());

        Button rateButton = findViewById(R.id.rateButton);
        rateButton.setOnClickListener(v -> rateApp());

    }

    private void contactDeveloper() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:developer@example.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FocusPomodoro Feedback");
        try {
            startActivity(emailIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void showPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.yourwebsite.com/privacy"));
        startActivity(browserIntent);
    }
}