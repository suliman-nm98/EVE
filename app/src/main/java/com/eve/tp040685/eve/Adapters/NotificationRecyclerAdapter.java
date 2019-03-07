package com.eve.tp040685.eve.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.Notification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    private List<Notification> notification_list;
    Context context;
    FirebaseFirestore firebaseFirestore;

    public NotificationRecyclerAdapter(Context context, List<Notification> notification_list){
        this.context = context;
        this.notification_list = notification_list;
    }
    @NonNull
    @Override
    public NotificationRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationRecyclerAdapter.ViewHolder viewHolder, int i) {

        final String from_id = notification_list.get(i).getFrom();
        viewHolder.notification_message.setText(notification_list.get(i).getMessage());

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(from_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot !=null) {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profile_image = documentSnapshot.getString("profile_image");
                        viewHolder.user_name.setText(name);
                        RequestOptions requestOptions = new RequestOptions();
                        if(profile_image !=null) {
                            requestOptions.placeholder(R.mipmap.ic_eve_logo);
                            CircleImageView user_image = viewHolder.user_profile_image;
                            user_image.setImageURI(Uri.parse(profile_image));
                            Glide.with(context).setDefaultRequestOptions(requestOptions).load(profile_image).into(user_image);
                        }
                    }
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return notification_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private  View mView;
        private CircleImageView user_profile_image;
        private TextView user_name, notification_message;

        public  ViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            user_profile_image = mView.findViewById(R.id.managers_list_user_image);
            user_name = mView.findViewById(R.id.managers_list_user_name);
            notification_message = mView.findViewById(R.id.notification_message);


        }

    }
}
