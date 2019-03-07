package com.eve.tp040685.eve;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eve.tp040685.eve.Adapters.MessagesRecyclerAdapter;
import com.eve.tp040685.eve.UserClasses.EventPost;
import com.eve.tp040685.eve.UserClasses.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String chat_user_id;
    private String chat_user_name;
    private String current_user_id;
    private Toolbar mToolbar;
    private FirebaseFirestore firebaseFirestore;
    private ImageView send_button;
    private TextView chat_message;
    private RecyclerView messages_recycler_view;
    private List<Message> messageList;
    private LinearLayoutManager linearLayout;
    private MessagesRecyclerAdapter messagesRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    CollectionReference messages_reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseFirestore = FirebaseFirestore.getInstance();
        chat_user_id = getIntent().getStringExtra("chat_user_id");
        chat_user_name = getIntent().getStringExtra("chat_user_name");
        send_button = findViewById(R.id.send_button);
        chat_message = findViewById(R.id.chat_message);
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        messages_recycler_view = findViewById(R.id.messages_recyclerView);
        messageList = new ArrayList<>();
        linearLayout = new LinearLayoutManager(this);

        messages_recycler_view.setHasFixedSize(true);
        messages_recycler_view.setLayoutManager(linearLayout);
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(messageList);
        messages_recycler_view.setAdapter(messagesRecyclerAdapter);

        mToolbar = findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(chat_user_name);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        messageList.clear();
        if(firebaseAuth.getCurrentUser()!=null) {
            getMessages();

            send_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessage();
                    chat_message.setText("");
                }
            });
        }


    }

    private void sendMessage() {
        final String message = chat_message.getText().toString();
        if(!TextUtils.isEmpty(message)){
            Map<String, Object> messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("from", current_user_id);
            firebaseFirestore.collection("Users").document(current_user_id).collection("Chats")
                    .document(chat_user_id).collection("Messages").add(messageMap);
            firebaseFirestore.collection("Users").document(chat_user_id).collection("Chats")
                    .document(current_user_id).collection("Messages").add(messageMap);
        }
    }

    private void getMessages(){
        firebaseFirestore.collection("Users").document(current_user_id).collection("Chats").document(chat_user_id)
                .collection("Messages").orderBy("timestamp").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots != null) {
                    if(!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                Message message = doc.getDocument().toObject(Message.class);
                                messageList.add(message);
                                messagesRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });
    }
}
