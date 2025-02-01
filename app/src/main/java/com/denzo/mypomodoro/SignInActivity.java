package com.denzo.mypomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        EditText email = findViewById(R.id.signin_email);
        EditText password = findViewById(R.id.signin_password);
        Button signInButton = findViewById(R.id.signin_button);
        TextView signUpRedirect = findViewById(R.id.signup_redirect);

        signInButton.setOnClickListener(v -> {
            String mail = email.getText().toString();
            String pass = password.getText().toString();

            if (mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(SignInActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignInActivity.this, "Sign In Successful!", Toast.LENGTH_SHORT).show();
            }
        });

        signUpRedirect.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
    }
}
