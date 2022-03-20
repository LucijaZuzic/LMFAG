package com.example.lmfag;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView myUsername = findViewById(R.id.textViewUsername);
        TextView myEmail = findViewById(R.id.textViewEmail);
        myEmail.setText("email: " + user.getEmail());
        myUsername.setText("username: " + user.getDisplayName());
    }
}