package com.eve.tp040685.eve.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eve.tp040685.eve.Adapters.MessagesListAdapter;
import com.eve.tp040685.eve.R;
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

public class MessagesFragment extends Fragment {

    private String chat_user_id;
    private String current_user_id;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView messages_list_recycler_view;
    List<String> users_list;
    private MessagesListAdapter messagesListAdapter;
    private FirebaseAuth firebaseAuth;
    public MessagesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid().toString();

        users_list = new ArrayList<>();
        messagesListAdapter = new MessagesListAdapter(getContext(), users_list);
        messages_list_recycler_view = view.findViewById(R.id.messages_list_recyclerView);
        messages_list_recycler_view.setHasFixedSize(true);
        messages_list_recycler_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        messages_list_recycler_view.setAdapter(messagesListAdapter);

        //clear the user list before fitching
        users_list.clear();
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
                                messagesListAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
