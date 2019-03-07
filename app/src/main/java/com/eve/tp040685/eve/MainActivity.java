package com.eve.tp040685.eve;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.eve.tp040685.eve.Fragments.HomeFragment;
import com.eve.tp040685.eve.Fragments.MyPostsFragment;
import com.eve.tp040685.eve.Fragments.MessagesFragment;
import com.eve.tp040685.eve.Fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class  MainActivity extends AppCompatActivity{
    private ImageView imgProfile;
    private DrawerLayout mDrawerLayout;
    private BottomNavigationView navigationView;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private BottomNavigationView mainNavMenu;
    private BottomNavigationView getMainNavMenu_student;
    private FirebaseFirestore firebaseFirestore;

    private HomeFragment homeFragment;
    private MessagesFragment messagesFragment;
    private ProfileFragment profileFragment;
    private MyPostsFragment myPostsFragment;
    private String role;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        //toolbar
        mToolbar = findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("EVE");
        homeFragment = new HomeFragment();
        messagesFragment = new MessagesFragment();
        profileFragment = new ProfileFragment();
        myPostsFragment = new MyPostsFragment();

        firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        role = task.getResult().getString("role");
                        setMainNavMenu(role);
                        initializeMain(role);
                    }

                }
            }
        });






//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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


    public void setMainNavMenu(String role){
        getMainNavMenu_student = findViewById(R.id.mainNavBar_student);
        mainNavMenu = findViewById(R.id.mainNavBar);
        if(role.equals("Student")){
            getMainNavMenu_student.setVisibility(View.VISIBLE);
        }
        if(role.equals("Manager")){
            mainNavMenu.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Exit!")
                    .setMessage("Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.finishAffinity(MainActivity.this);
                            finish();
                        }
                    }).show();
    }



    //Signout the user from app
    private void logout(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout!")
                .setMessage("Are you sure?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        mAuth.signOut();
                    }
                }).setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    private void switchFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(profileFragment);
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(myPostsFragment);
        }

        if(fragment == profileFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(myPostsFragment);

        }

        if(fragment == messagesFragment){
            fragmentTransaction.hide(myPostsFragment);
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(profileFragment);
        }

        if(fragment == myPostsFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(profileFragment);
        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

    private void initializeFragment(String role){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(role.equals("Student")) {
            fragmentTransaction.add(R.id.main_container, homeFragment);
            fragmentTransaction.add(R.id.main_container, messagesFragment);
            fragmentTransaction.add(R.id.main_container, profileFragment);
            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(profileFragment);
            fragmentTransaction.commit();
        }
        else if(role.equals("Manager")){
            fragmentTransaction.add(R.id.main_container, homeFragment);
            fragmentTransaction.add(R.id.main_container, messagesFragment);
            fragmentTransaction.add(R.id.main_container, profileFragment);
            fragmentTransaction.add(R.id.main_container, myPostsFragment);

            fragmentTransaction.hide(messagesFragment);
            fragmentTransaction.hide(myPostsFragment);
            fragmentTransaction.hide(profileFragment);
            fragmentTransaction.commit();
        }
        else{

        }


    }

    private void showNoInternetDialog(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("No internet connection!")
                .setCancelable(true)
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isConnected(MainActivity.this)){
                            showNoInternetDialog();
                        }
                    }
                }).show();
    }

    public void connectAdmin(){
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
        } else
        return false;
    }

    private void initializeMain(String role){
        if(isConnected(MainActivity.this)) {
            if (mAuth.getCurrentUser() != null) {
                if (role.equals("Manager")) {
                    //Fragments

                    initializeFragment(role);
                    //
                    mainNavMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                            switch (menuItem.getItemId()) {
                                case R.id.ic_frag_home:
                                    switchFragment(homeFragment, currentFragment);
                                    return true;

                                case R.id.ic_frag_my_posts:
                                    switchFragment(myPostsFragment, currentFragment);
                                    return true;

                                case R.id.ic_frag_new_post:
                                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.ic_frag_messages:
                                    switchFragment(messagesFragment, currentFragment);
                                    return true;
                                case R.id.ic_frag_profile:
                                    switchFragment(profileFragment, currentFragment);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                }
                else if(role.equals("Student")){
                    initializeFragment(role);
                    //
                    getMainNavMenu_student.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                            switch (menuItem.getItemId()) {
                                case R.id.ic_frag_home:
                                    switchFragment(homeFragment, currentFragment);
                                    return true;
                                case R.id.ic_frag_messages:
                                    switchFragment(messagesFragment, currentFragment);
                                    return true;
                                case R.id.ic_frag_profile:
                                    switchFragment(profileFragment, currentFragment);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                }
                else if(role.equals("Admin")){
                        connectAdmin();
                }
                else{ logout();}
            } else {
                logout();
            }
        }
        else{
            showNoInternetDialog();
        }
    }

}
