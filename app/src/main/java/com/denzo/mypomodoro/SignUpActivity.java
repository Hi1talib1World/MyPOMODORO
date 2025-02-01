package com.denzo.mypomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText username = findViewById(R.id.signup_username);
        EditText email = findViewById(R.id.signup_email);
        EditText password = findViewById(R.id.signup_password);
        Button signUpButton = findViewById(R.id.signup_button);
        TextView loginRedirect = findViewById(R.id.login_redirect);

        signUpButton.setOnClickListener(v -> {
            String user = username.getText().toString();
            String mail = email.getText().toString();
            String pass = password.getText().toString();

            if (user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

        loginRedirect.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));
    }
}
