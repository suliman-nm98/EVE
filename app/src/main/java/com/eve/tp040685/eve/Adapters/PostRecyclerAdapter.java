package com.eve.tp040685.eve.Adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eve.tp040685.eve.ChatActivity;
import com.eve.tp040685.eve.EditPostActivity;
import com.eve.tp040685.eve.MainActivity;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.EventPost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>{

    private List<EventPost> post_list;
    Context context;
    private  FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String post_user_name;
    private String post_user_image;
    public  PostRecyclerAdapter(Context context, List<EventPost> post_list){
        this.post_list = post_list;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list_item, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostRecyclerAdapter.ViewHolder viewHolder, final int position) {

        viewHolder.setIsRecyclable(false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final String event_description_text = post_list.get(position).getDescription();
        viewHolder.setDecriptionView(event_description_text);

        final String eventPostId = post_list.get(position).EventPostId;
        final String current_user_id = firebaseAuth.getCurrentUser().getUid().toString();
        final String post_user_id = post_list.get(position).getUserId().toString();

        final String event_title = post_list.get(position).getEvent_title();
        viewHolder.setEventTitle(event_title);

        final String image_url = post_list.get(position).getImageUrl();
        final String thumbUri = post_list.get(position).getImageThumbUrl();
        viewHolder.setPostImage(image_url, thumbUri);

        final String event_venue = post_list.get(position).getEventVenue();
        viewHolder.setEventVenue(event_venue);

        final String event_end_date = post_list.get(position).getEventEndDate();
        final String event_start_date = post_list.get(position).getEventStartDate();
        viewHolder.setEventDate(event_start_date, event_end_date);

        try {
            long millisecond = post_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM-dd-yyyy", new Date(millisecond)).toString();
            viewHolder.setPublishDate(dateString);
        }
        catch (Exception e){}



            final String user_id = post_list.get(position).getUserId();
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            post_user_name = task.getResult().getString("name");
                            post_user_image = task.getResult().getString("profile_image");
                            viewHolder.setUsername(post_user_name);
                            viewHolder.setUserImage(post_user_image);
                        }
                    } else {

                    }
                }
            });
        //get likes count
            firebaseFirestore.collection("Posts/" + eventPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            int count = documentSnapshots.size();
                            viewHolder.updateLikesCount(count);
                        }
                        else {
                            viewHolder.updateLikesCount(0);
                        }
                    }
                }
            });
            //likes Feature
            firebaseFirestore.collection("Posts/" + eventPostId + "/Likes").document(current_user_id)
                    .addSnapshotListener( new EventListener<DocumentSnapshot>() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if(documentSnapshot.exists()){
                            viewHolder.like_button.setImageDrawable(context.getDrawable(R.mipmap.like_button_liked));
                         } else {
                            viewHolder.like_button.setImageDrawable(context.getDrawable(R.mipmap.like_button));
                        }
                    }
                }
            });

            //like post
            viewHolder.like_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    firebaseFirestore.collection("Posts/" + eventPostId + "/Likes").document(current_user_id).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts/" + eventPostId + "/Likes").document(current_user_id).set(likesMap);
                            } else {
                                firebaseFirestore.collection("Posts/" + eventPostId + "/Likes").document(current_user_id).delete();
                            }
                        }
                    });


                }
            });
            //send message
        viewHolder.message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chat_user_id",post_user_id);
                intent.putExtra("chat_user_name",post_user_name);
                intent.putExtra("current_user_id",current_user_id);
                context.startActivity(intent);
            }
        });


            //edit post
            if(current_user_id.equals(post_user_id)) {
                viewHolder.message_button.setVisibility(View.INVISIBLE);
                viewHolder.post_options.setVisibility(View.VISIBLE);
                viewHolder.post_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(context, viewHolder.post_options);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.post_menu_options);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.ic_action_post_edit:
                                        Intent intent = new Intent(context, EditPostActivity.class);
                                        intent.putExtra("image_url", image_url);
                                        intent.putExtra("thumb_uri", thumbUri);
                                        intent.putExtra("event_title", event_title);
                                        intent.putExtra("event_start_date", event_start_date);
                                        intent.putExtra("event_end_date", event_end_date);
                                        intent.putExtra("event_venue", event_venue);
                                        intent.putExtra("event_description", event_description_text);
                                        intent.putExtra("event_post_id", eventPostId);
                                        context.startActivity(intent);

                                        return true;
                                    case R.id.ic_action_post_delete:
                                        post_list.remove(position);
                                        firebaseFirestore.collection("Posts").document(eventPostId).delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(context,"Event removed.", Toast.LENGTH_LONG).show();
                                                }
                                                else{
                                                    Toast.makeText(context,"Error occurred please try again!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        //displaying the popup
                        popup.show();

                    }
                });
            }
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView decriptionView;
        private ImageView postImageView;
        private TextView event_title, event_venue, publish_date, user_name, event_like_count, event_start_date, event_end_date;
        private ImageView like_button, share_button, message_button;
        private CircleImageView user_profile_image;
        private ImageView post_options;
        private RequestOptions requestOptions = new RequestOptions();

        public ViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            like_button = mView.findViewById(R.id.likeBtn);
            post_options = mView.findViewById(R.id.post_options_imageView);
            post_options.setVisibility(View.INVISIBLE);
            message_button = mView.findViewById(R.id.messageBtn);
        }

        public void setDecriptionView(String descriptionText){
            decriptionView = mView.findViewById(R.id.post_description);
            decriptionView.setText(descriptionText);
        }
        public void setPostImage(String downloadUri, String thumbUri){
            postImageView = mView.findViewById(R.id.post_image);
            requestOptions.placeholder(R.mipmap.ic_eve_logo);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri)
                    .thumbnail( Glide.with(context).load(thumbUri)).into(postImageView);

        }
        public  void setEventTitle(String event_titleText){
            event_title = mView.findViewById(R.id.new_post_event_title);
            event_title.setText(event_titleText);
        }
        public void setEventVenue(String event_venue){
            this.event_venue = mView.findViewById(R.id.post_event_venue);
            this.event_venue.setText(event_venue);
        }
        public void setUserImage(String user_image){
            if(user_image !=null) {
                requestOptions.placeholder(R.mipmap.ic_eve_logo);
                this.user_profile_image = mView.findViewById(R.id.post_user_image);
                Glide.with(context).applyDefaultRequestOptions(requestOptions).load(user_image).into(user_profile_image);
            }

        }
        public void  setUsername(String user_name){
            this.user_name = mView.findViewById(R.id.post_username);
            this.user_name.setText(user_name);
        }
        public void setPublishDate(String date){
            publish_date = mView.findViewById(R.id.post_publish_date);
            publish_date.setText(date);
        }

        public void updateLikesCount(int count){
            event_like_count = mView.findViewById(R.id.likeCount);
            event_like_count.setText(count + " Likes");
        }

        public void setEventDate(String event_start_date, String event_end_date) {
            this.event_start_date = mView.findViewById(R.id.post_start_date);
            this.event_start_date.setText(event_start_date);
            this.event_end_date = mView.findViewById(R.id.post_end_date);
            this.event_end_date.setText(event_end_date);
        }
    }
}
