package com.eve.tp040685.eve.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eve.tp040685.eve.Adapters.NotificationRecyclerAdapter;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.Notification;
import com.eve.tp040685.eve.UserClasses.Student;
import com.eve.tp040685.eve.UserClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView user_name, user_email;
    private TextView btn_changePhoto;
    private Uri filePath = null;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar mProgressBar;
    private String user_id;
    private TabHost host;
    private List<Notification> notification_list;
    private RecyclerView notification_recyclerView;
    private NotificationRecyclerAdapter notificationRecyclerAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        imgProfile = view.findViewById(R.id.imgProfile);
        user_name = view.findViewById(R.id.user_name);
        user_email = view.findViewById(R.id.user_email);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        btn_changePhoto = (TextView) view.findViewById(R.id.txt_btn_change_photo);
        user_id = firebaseAuth.getCurrentUser().getUid();

        host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Notifications");
        spec.setContent(R.id.tab_messages);
        spec.setIndicator("Notifications");
        host.addTab(spec);
        notification_list = new ArrayList<>();
        notificationRecyclerAdapter = new NotificationRecyclerAdapter(container.getContext(),notification_list);
        notification_recyclerView = view.findViewById(R.id.notification_recyclerView);
        notification_recyclerView.setHasFixedSize(true);
        notification_recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        notification_recyclerView.setAdapter(notificationRecyclerAdapter);


        notification_list.clear();
        firebaseFirestore.collection("Users").document(user_id).collection("Notifications")
                .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots != null) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Notification notification = doc.getDocument().toObject(Notification.class);
                            notification_list.add(notification);
                            notificationRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        //Tab 2
        spec = host.newTabSpec("tab_Profile");
        spec.setContent(R.id.tab_likes);
        spec.setIndicator("Profile");
        host.addTab(spec);


        btn_changePhoto.setEnabled(false);
        //get user profile photo
        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().exists()){
                        String image = task.getResult().getString("profile_image");
                        user_name.setText("Name: "+task.getResult().getString("name"));
                        user_email.setText("Email: "+ task.getResult().getString("username"));
                        if(image != null) {
                            filePath = Uri.parse(image);
                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.mipmap.ic_eve_logo);
                            Glide.with(getActivity().getApplicationContext()).load(image).into(imgProfile);
                        }
                    }

                }
                else{

                    String error = task.getException().getMessage();
                    Toast.makeText(getActivity(),"Firebase Retrieve Error :"+error, Toast.LENGTH_LONG).show();

                }
                mProgressBar.setVisibility(View.INVISIBLE);
                btn_changePhoto.setEnabled(true);
            }
        });
        //upload profile picture
        btn_changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else
                    {
                        setImagePicker();
                    }
                }
                else{

                    setImagePicker();

                }
            }
        });

        return view;

    }


    //open Image picker activity
    private void setImagePicker() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .setMinCropResultSize(345,200)
                .start(getContext(),this);
        mProgressBar.setVisibility(View.VISIBLE);
    }
    //save profile Image to Firebase
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                imgProfile.setImageURI(filePath);
                if(filePath !=null) {
                    final String user_id = firebaseAuth.getCurrentUser().getUid();
                    //saves on image for user and the image is replaced with the previos image
                    StorageReference image_path = mStorageRef.child("profile_images").child(user_id+".jpg");
                    image_path.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                final String downloadUri = task.getResult().getDownloadUrl().toString();
                            if (task.isSuccessful()) {
                                Uri download_uri = task.getResult().getDownloadUrl();
                                User student = new Student(download_uri.toString());
                                firebaseFirestore.collection("Users").document(user_id).update("profile_image",
                                        download_uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()){
                                            String error = task.getException().getMessage();
                                            Toast.makeText(getActivity(),"Firebase Error :"+error, Toast.LENGTH_LONG).show();
                                        }
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(getActivity().getApplicationContext(), "Error : " +
                                        error, Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }


                        }
                    });
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
