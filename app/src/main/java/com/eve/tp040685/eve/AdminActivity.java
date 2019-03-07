package com.eve.tp040685.eve;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eve.tp040685.eve.Adapters.ManagersRecyclerAdapter;
import com.eve.tp040685.eve.UserClasses.Manager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    List<String> managers;
    FirebaseAuth firebaseAuth;
    private String current_user_id;
    private RecyclerView managers_recyclerView;
    FirebaseFirestore firebaseFirestore;
    private String username;
    private String password;
    private ManagersRecyclerAdapter managersRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        if(firebaseAuth.getCurrentUser() !=null) {

            current_user_id = firebaseAuth.getCurrentUser().getUid();

            managers = new ArrayList<>();
            managersRecyclerAdapter = new ManagersRecyclerAdapter(this, managers);
            managers_recyclerView = findViewById(R.id.managers_recyclerView);
            managers_recyclerView.setHasFixedSize(true);
            managers_recyclerView.setLayoutManager(new LinearLayoutManager(this));
            managers_recyclerView.setAdapter(managersRecyclerAdapter);
            getManagers();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_manager);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AdminActivity.this, RegisterManagerActivity.class);
                    intent.putExtra("username",username);
                    intent.putExtra("password",password);
                    startActivity(intent);
                }
            });

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            username = task.getResult().getString("username");
                            password = task.getResult().getString("password");
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout_btn:
                logout();
                return true;
            default:
                return false;
        }
    }
    //Signout the user from app
    private void logout(){
        new AlertDialog.Builder(AdminActivity.this)
                .setTitle("Logout!")
                .setMessage("Are you sure?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        firebaseAuth.signOut();
                    }
                }).setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }



    public void getManagers(){
        managers.clear();
        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots !=null) {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                final String user_id = doc.getDocument().getId().toString();
                                firebaseFirestore.collection("Users").document(user_id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(task.getResult().exists()){
                                                String role = task.getResult().getString("role");
                                                if(role.equals("Manager")){
                                                    managers.add(user_id);
                                                    managersRecyclerAdapter.notifyDataSetChanged();
                                                }

                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

}
