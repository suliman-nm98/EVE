package com.eve.tp040685.eve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.Manager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ManagersRecyclerAdapter extends RecyclerView.Adapter<ManagersRecyclerAdapter.ViewHolder>{
    List<String> managers;
    Context context;
    FirebaseFirestore firebaseFirestore;

    public ManagersRecyclerAdapter(Context context, List<String> managers) {
        this.managers = managers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.managers_list_item, viewGroup, false);
        return new ManagersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        //get user details
        firebaseFirestore = FirebaseFirestore.getInstance();

        final String manager_id = managers.get(i).toString();
        firebaseFirestore.collection("Users").document(manager_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot !=null) {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("username");
                        String profile_image = documentSnapshot.getString("profile_image");
                        String id = documentSnapshot.getString("id");
                        viewHolder.setUserId(id,i);
                        viewHolder.setUserName(name, email);
                        RequestOptions requestOptions = new RequestOptions();
                        if(profile_image !=null) {
                            requestOptions.placeholder(R.mipmap.ic_eve_logo);
                            viewHolder.setProfileImage(profile_image);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return managers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_name, user_email;
        CircleImageView profile_image;
        RequestOptions requestOptions = new RequestOptions();
        String user_id;
        int position;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    removeUser(position,user_id, user_name.getText().toString());
                    managers.remove(position);
                    return true;
                }
            });
            user_name = itemView.findViewById(R.id.managers_list_user_name);
            user_email= itemView.findViewById(R.id.managers_list_user_email);
            profile_image = itemView.findViewById(R.id.managers_list_user_image);
        }
        public void setUserId(String user_id, int position){
            this.user_id = user_id;
            this.position = position;
        }
        public void removeUser(final int position, final String id, final String username){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog);
            } else {
                builder = new AlertDialog.Builder(context);
            }
            builder.setTitle("Delete " +username+"!")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton(R.string.str_delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseFirestore.collection("Users").document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context,username+ " removed successfully", Toast.LENGTH_LONG).show();
                                        notifyDataSetChanged();
                                    }
                                    else{
                                        Toast.makeText(context,"Error occurred please try again!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        public void setProfileImage(String sender_profile_image){
            requestOptions.placeholder(R.mipmap.ic_eve_logo);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(sender_profile_image).into(profile_image);
        }
        public void setUserName(String name, String email){
            user_name.setText(name);
            user_email.setText(email);
        }
    }
}
