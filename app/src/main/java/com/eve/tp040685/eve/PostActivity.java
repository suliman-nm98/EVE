package com.eve.tp040685.eve;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Notification;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    Toolbar mToolbar;
    TextView eventDescription, eventTitle, eventStartDate, eventEndDate;
    String eventVenue;
    ImageView eventImage;

    Button postBtn;
    List<String> users_list;

    private Uri postImageUri = null;
    private ProgressBar mProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    Calendar myCalendar;
    private DatabaseReference usersReference;

    private String current_user_id;
    private String user_firstname;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        eventTitle = findViewById(R.id.new_post_event_title);
        eventStartDate = findViewById(R.id.new_post_event_start_date);
        eventEndDate = findViewById(R.id.new_post_event_end_date);
        eventImage = findViewById(R.id.new_post_Image);
        eventDescription = findViewById(R.id.new_post_description);
        myCalendar = Calendar.getInstance();
        users_list = new ArrayList<>();

        mProgressBar = findViewById(R.id.progressBar);
        mToolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        //set Event Image
        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PostActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        setImagePicker();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                } else {

                    setImagePicker();
                    mProgressBar.setVisibility(View.INVISIBLE);

                }
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
                            if(doc.getType()== DocumentChange.Type.MODIFIED){

                            }
                        }
                    }
                }
            }
        });

        //get user_name
        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        user_firstname = task.getResult().getString("name");
                    }
                }
            }
        });

        //publish event
        postBtn = (Button) findViewById(R.id.postEvent);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postBtn.setEnabled(false);
                publishPost();
            }
        });
        //pick event start date
        eventStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(PostActivity.this, R.style.DialogTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                updateLabel(eventStartDate);
            }
        });
        //pick event end date
        eventEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(PostActivity.this, R.style.DialogTheme, date, myCalendar
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
            postBtn.setEnabled(true);
            return;
        }
        if(TextUtils.isEmpty(startDate) ){
            mProgressBar.setVisibility(View.GONE);
            eventStartDate.setError(getString(R.string.str_null_start_date));
            eventStartDate.requestFocus();
            postBtn.setEnabled(true);

            return;
        }
        if(TextUtils.isEmpty(endDate) ){
            mProgressBar.setVisibility(View.GONE);
            eventEndDate.setError(getString(R.string.str_null_end_date));
            eventTitle.requestFocus();
            postBtn.setEnabled(true);

            return;
        }
        if(TextUtils.isEmpty(venue) ){
            mProgressBar.setVisibility(View.GONE);
            eventEndDate.setError(getString(R.string.str_null_venue));
            eventTitle.requestFocus();
            postBtn.setEnabled(true);

            return;
        }
        if (postImageUri == null){
            mProgressBar.setVisibility(View.GONE);
            eventDescription.setError(getString(R.string.str_null_event_image));
            eventImage.requestFocus();
            postBtn.setEnabled(true);
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        final String random_image_name = UUID.randomUUID().toString();

        final StorageReference filepath = storageReference.child("post_images").child(random_image_name + ".jpg");
        filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final String downloadUri = task.getResult().getDownloadUrl().toString();
                if (task.isSuccessful()) {
                    File newImageFile = new File(postImageUri.getPath());
                    try {
                        compressedImageFile = new Compressor(PostActivity.this)
                                .setMaxHeight(365)
                                .setMaxWidth(256)
                                .setQuality(1).compressToBitmap(newImageFile);

                    }
                    catch (Exception e){}

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                    byte[] thumbData = baos.toByteArray();

                    UploadTask uploadTask = storageReference.child("post_images/thumbs").child(random_image_name+".jpg")
                            .putBytes(thumbData);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();
                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("user_id", current_user_id);
                            postMap.put("event_title",title);
                            postMap.put("event_start_date",startDate);
                            postMap.put("event_end_date",endDate);
                            postMap.put("event_venue",venue);
                            postMap.put("description", desc);
                            postMap.put("timestamp", FieldValue.serverTimestamp());
                            postMap.put("image_url", downloadUri);
                            postMap.put("image_thumb_url", downloadthumbUri);


                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PostActivity.this, "Event added", Toast.LENGTH_SHORT).show();
                                        pushNotification(eventTitle.getText().toString()+" Is a new event");
                                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                    }
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    //open Image picker activity
    private void setImagePicker() {
        mProgressBar.setVisibility(View.VISIBLE);
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setMinCropResultSize(512, 512)
                .start(PostActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                eventImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    protected  void pushNotification(final String message){
        Map<String, Object> notificationMessage = new HashMap<>();
        for(int i=0; i<users_list.size(); i++){
            notificationMessage.put("message",message);
            notificationMessage.put("from", current_user_id);
            firebaseFirestore.collection("Users/" +users_list.get(i)+ "/Notifications").add(notificationMessage);
        }
    }
}
