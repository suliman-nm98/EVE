package com.eve.tp040685.eve;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.eve.tp040685.eve.UserClasses.Student;
import com.eve.tp040685.eve.UserClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity{
    private EditText input_name, input_email,input_password, input_confirm_password;
    private Button btn_signup;
    private String id;
    Calendar myCalendar;

    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //firebase authentication
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //DOB date picker
        myCalendar = Calendar.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //variable instantiation
        input_name = (EditText) findViewById(R.id.input_name);
        input_email = (EditText) findViewById(R.id.input_username);
        input_password = (EditText)findViewById(R.id.input_password);
        input_confirm_password =(EditText) findViewById(R.id.input_confirm_password);
        btn_signup = (Button) findViewById(R.id.btn_signup);

        mToolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("EVE");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



//        //DOB date picker
//        input_DOB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO Auto-generated method stub
//                new DatePickerDialog(RegistrationActivity.this, R.style.DialogTheme, date, myCalendar
//                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
//            }
//        });

        //Create new User
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show progress bar
                progressBar.setVisibility(View.VISIBLE);
                registerUser();
            }
        });

    }

    //validate password
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    //validate password
    public static boolean isValidEmail(final String email) {

        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|" +
                "\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])" +
                "*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4]" +
                "[0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);

        return matcher.matches();

    }



    //Register user
    private void registerUser(){

        final String name = input_name.getText().toString().trim();
        final String username = input_email.getText().toString().toLowerCase().trim();
        final String password = input_password.getText().toString().trim();
        final String confirmPassword = input_confirm_password.getText().toString().trim();
        final String role = "Student";

        if(TextUtils.isEmpty(name) ){
            progressBar.setVisibility(View.GONE);
            input_name.setError(getString(R.string.str_null_name));
            input_name.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(username) && !isValidEmail(username)){
            progressBar.setVisibility(View.GONE);
            input_email.setError(getString(R.string.str_inval_email));
            input_email.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password)){
            progressBar.setVisibility(View.GONE);
            input_password.setError(getString(R.string.str_null_password));
            input_password.requestFocus();
            return;
        }

        if(password.length() < 8 && !isValidPassword(password)){
            progressBar.setVisibility(View.GONE);
            input_password.setError(getString(R.string.str_inval_password));
            input_password.requestFocus();
            return;
        }
        if(confirmPassword.length() < 8 && !isValidPassword(confirmPassword)){
            progressBar.setVisibility(View.GONE);
            input_confirm_password.setError(getString(R.string.str_password_notMatching));
            input_confirm_password.requestFocus();
            return;
        }
        if(!password.equals(confirmPassword))
        {
            progressBar.setVisibility(View.GONE);
            input_confirm_password.setError(getString(R.string.str_password_notMatching));
            input_confirm_password.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //add user to Firebase
                            String id = mAuth.getCurrentUser().getUid();
                            User student = new Student(id,name,username,role,password);
                           firebaseFirestore.collection("Users").document(id).set(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(!task.isSuccessful()){
                                       Toast.makeText(RegistrationActivity.this, "Authentication failed."
                                                       + task.getException().getMessage().toString(),
                                               Toast.LENGTH_SHORT).show();
                                       return;
                                   }
                                   loadMainActivity();
                                   progressBar.setVisibility(View.INVISIBLE);
                               }
                           });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            if(!isValidEmail(username)){
                                input_email.setError(getString(R.string.str_inval_email));
                                input_email.requestFocus();
                                return;
                            }
                            Toast.makeText(RegistrationActivity.this, "Authentication failed."
                                            + task.getException().getMessage().toString(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ...
                    }
                });

    }

    //Load Main Activity
    public void loadMainActivity(){
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
