package com.eve.tp040685.eve.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessagesRecyclerAdapter.ViewHolder>{

    List<Message> messagesList;
    private FirebaseAuth mAuth;

    public MessagesRecyclerAdapter(List<Message> messagesList){
        this.messagesList = messagesList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_single_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        Message  c = messagesList.get(i);
        String from_user = c.getFrom();
        if(from_user.equals(current_user_id)){
            viewHolder.message_text.setBackgroundColor(Color.WHITE);
            viewHolder.message_text.setTextColor(Color.BLACK);
        }
        else{
            viewHolder.message_text.setBackgroundResource(R.drawable.message_text_background_shape);
            viewHolder.message_text.setTextColor(Color.WHITE);
        }
        viewHolder.message_text.setText(c.getMessage());

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView message_text;
        CircleImageView profile_image;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                message_text = itemView.findViewById(R.id.message_text);
                profile_image = itemView.findViewById(R.id.message_user_profile_image);
            }
        }

    }
