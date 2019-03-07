package com.eve.tp040685.eve;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class EditPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView eventDescription, eventTitle, eventStartDate, eventEndDate;
    private String eventVenue;
    private ImageView eventImage;
    private Button saveBtn;
    private ProgressBar mProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference usersReference;
    private FirebaseAuth firebaseAuth;
    private List<String> users_list;
    private Bundle bundle;
    private String eventPostId;

    private Calendar myCalendar;
    private String current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);


        eventTitle = findViewById(R.id.new_post_event_title);
        eventStartDate = findViewById(R.id.new_post_event_start_date);
        eventEndDate = findViewById(R.id.new_post_event_end_date);
        eventImage = findViewById(R.id.new_post_Image);
        users_list = new ArrayList<>();
        eventDescription = findViewById(R.id.new_post_description);
        myCalendar = Calendar.getInstance();
        if(savedInstanceState ==null) {
            bundle = getIntent().getExtras();
            if(bundle != null) {
                eventTitle.setText(bundle.getString("event_title"));
                eventStartDate.setText(bundle.getString("event_start_date"));
                eventEndDate.setText(bundle.getString("event_end_date"));
                eventDescription.setText(bundle.getString("event_description"));
                eventVenue = bundle.getString("event_venue");
                eventPostId = bundle.getString("event_post_id");
            }

        }

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        mProgressBar = findViewById(R.id.progressBar);
        mToolbar = findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Post");

        Spinner staticSpinner = (Spinner) findViewById(R.id.new_post_venue_spinner);

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.brew_array,
                        android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        staticSpinner.setAdapter(staticAdapter);
        staticSpinner.setSelection(staticAdapter.getPosition(eventVenue));

        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                eventVenue =  (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        //clear the user list before fitching
        users_list.clear();
        //get all users
        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots !=null) {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String user_id = doc.getDocument().getId().toString();
                                users_list.add(user_id);
                                users_list.remove(current_user_id);
                            }
                        }
                    }
                }
            }
        });

        saveBtn = (Button) findViewById(R.id.saveEvent);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBtn.setEnabled(false);
                publishPost();
            }
        });

        eventStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditPostActivity.this, R.style.DialogTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                updateLabel(eventStartDate);
            }
        });
        eventEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditPostActivity.this, R.style.DialogTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                updateLabel(eventEndDate);
            }
        });

    }
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

    };

    private void updateLabel(TextView textView) {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        textView.setText(sdf.format(myCalendar.getTime()));
    }
    private void publishPost(){
        final String title = eventTitle.getText().toString().trim();
        final String desc = eventDescription.getText().toString().trim();
        final String startDate = eventStartDate.getText().toString().trim();
        final String endDate = eventEndDate.getText().toString().trim();
        final String venue = eventVenue;

        if(TextUtils.isEmpty(title) ){
            mProgressBar.setVisibility(View.GONE);
            eventTitle.setError(getString(R.string.str_null_title));
            eventTitle.requestFocus();
            saveBtn.setEnabled(true);
            return;
        }
        if(TextUtils.isEmpty(startDate) ){
            mProgressBar.setVisibility(View.GONE);
            eventStartDate.setError(getString(R.string.str_null_start_date));
            eventStartDate.requestFocus();
            saveBtn.setEnabled(true);

            return;
        }
        if(TextUtils.isEmpty(endDate) ){
            mProgressBar.setVisibility(View.GONE);
            eventEndDate.setError(getString(R.string.str_null_end_date));
            eventTitle.requestFocus();
            saveBtn.setEnabled(true);

            return;
        }
        if(TextUtils.isEmpty(venue) ){
            mProgressBar.setVisibility(View.GONE);
            eventEndDate.setError(getString(R.string.str_null_venue));
            eventTitle.requestFocus();
            saveBtn.setEnabled(true);
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        final String random_image_name = UUID.randomUUID().toString();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("user_id", current_user_id);
        postMap.put("event_title",title);
        postMap.put("event_start_date",startDate);
        postMap.put("event_end_date",endDate);
        postMap.put("event_venue",venue);
        postMap.put("description", desc);

        firebaseFirestore.collection("Posts").document(eventPostId).update(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(EditPostActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                });
        }

    //open Image picker activity
    private void setImagePicker() {
        mProgressBar.setVisibility(View.VISIBLE);
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setMinCropResultSize(512, 512)
                .start(EditPostActivity.this);
    }

}
