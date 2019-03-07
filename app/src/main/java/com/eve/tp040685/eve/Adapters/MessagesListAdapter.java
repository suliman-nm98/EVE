package com.eve.tp040685.eve.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eve.tp040685.eve.ChatActivity;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {
    private List<String> users_list;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Context context;

    public MessagesListAdapter(Context context,List<String> users_list){
        this.context = context;
        this.users_list = users_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.messages_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        messageList = new ArrayList<>();
        final String chat_user_name;
        String current_user_id = firebaseAuth.getCurrentUser().getUid();
        final String chat_user_id = users_list.get(i).toString();
        //get messages
        messageList.clear();
        firebaseFirestore.collection("Users").document(current_user_id).collection("Chats").document(chat_user_id)
                .collection("Messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    Message message = doc.getDocument().toObject(Message.class);
                                    messageList.add(i,message);
                                }
                                catch (Exception ex){}
                            }

                        }
//                        viewHolder.setLastMessage(i);
                    }
                }
            }
        });

        //get user details
        firebaseFirestore.collection("Users").document(chat_user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot !=null) {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profile_image = documentSnapshot.getString("profile_image");
                        viewHolder.setUser_name(name);
                        viewHolder.setChat_user_id(chat_user_id);
                        RequestOptions requestOptions = new RequestOptions();
                        if(profile_image !=null) {
                            requestOptions.placeholder(R.mipmap.ic_eve_logo);
                            viewHolder.setSender_profile_image(profile_image);
                        }
                    }
                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return users_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView user_name, message_text;
        String chat_user_name;
        String chat_user_id;
        CircleImageView profile_image;
        RequestOptions requestOptions = new RequestOptions();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chat_user_id",chat_user_id);
                    intent.putExtra("chat_user_name",chat_user_name);
                    context.startActivity(intent);
                }
            });

            user_name = itemView.findViewById(R.id.managers_list_user_name);
            message_text = itemView.findViewById(R.id.message);
            profile_image = itemView.findViewById(R.id.managers_list_user_image);
        }
        public void setChat_user_id(String id){
            chat_user_id = id;
        }
        public void setSender_profile_image(String sender_profile_image){
            requestOptions.placeholder(R.mipmap.ic_eve_logo);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(sender_profile_image).into(profile_image);
        }
        public void setUser_name(String name){
            user_name.setText(name);
            chat_user_name = name;
        }
        public void setLastMessage(int i){
            if(messageList.size()>0) {
                message_text.setText(messageList.get(i).getMessage());
            }
        }
    }
}
