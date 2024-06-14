package com.example.horizonconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogIn extends AppCompatActivity {

    EditText username, password;
    ProgressBar progressBar;
    Button loginBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        username = findViewById(R.id.userName);
        password = findViewById(R.id.pwd);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        if (usernameStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(LogIn.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);


        // Assuming you used this format for usernames as emails
        String email = usernameStr + "@horizoncampus.edu.lk";
        mAuth.signInWithEmailAndPassword(email, passwordStr)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Check if current user is not null
                            if (mAuth.getCurrentUser() != null) {
                                fetchUserRole();
                            } else {
                                Toast.makeText(LogIn.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LogIn.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchUserRole() {
        String userId = mAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String role = document.getString("role");
                                navigateToRoleSpecificActivity(role);
                            } else {
                                Toast.makeText(LogIn.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LogIn.this, "Failed to fetch user document: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToRoleSpecificActivity(String role) {
        if (role == null) {
            Toast.makeText(this, "Role not assigned", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        switch (role) {
            case "student":
                intent = new Intent(LogIn.this, StudentDashBoard.class);
                break;
            case "lecturer":
                intent = new Intent(LogIn.this, LecturerDashBoard.class);
                break;
            case "coordinator":
                intent = new Intent(LogIn.this, CoordinatorDashBoard.class);
                break;
            default:
                Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }
}
