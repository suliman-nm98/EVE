package com.eve.tp040685.eve;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity{
    private EditText input_username, input_password;

    private TextView btn_signUP;
    private FirebaseAuth mAuth;
    private Button btn_login;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        btn_signUP = (TextView) findViewById(R.id.txt_btn_signUp);
        input_username = (EditText) findViewById(R.id.input_username);
        input_password = (EditText) findViewById(R.id.input_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_login = (Button) findViewById(R.id.btn_login);
        mAuth = FirebaseAuth.getInstance();

        //check wether the app is used for the first time / loads the main page if user has logged in already
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show progress bar
                progressBar.setVisibility(View.VISIBLE);
                AuthenticateUser();
            }
        });
        btn_signUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);

            }
        });




    }

    //authenticate user with email and password Firebase
    private void AuthenticateUser(){

        final String username = input_username.getText().toString().trim();
        final String password = input_password.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) && TextUtils.isEmpty(password)){
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this,"Enter credentials!", Toast.LENGTH_SHORT).show();
            return;
        }

        //authenticate user
        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if Sign in fails, display a message to user.
                        // signed in user can be handled in the listener.
                        if(task.isSuccessful()){
                            //start main activity
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                                progressBar.setVisibility(View.GONE);
                            if(password.length() <8){
                                input_password.setError(getString(R.string.str_minimum_password));
                            }
                            else{
                                Toast.makeText(LoginActivity.this, getString(R.string.str_auth_failed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
