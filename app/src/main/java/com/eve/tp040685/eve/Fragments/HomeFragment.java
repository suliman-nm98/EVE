package com.eve.tp040685.eve.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eve.tp040685.eve.Adapters.PostRecyclerAdapter;
import com.eve.tp040685.eve.R;
import com.eve.tp040685.eve.UserClasses.EventPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView post_listview;
    private List<EventPost> post_list;

    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoaded = true;
    private FirebaseAuth mAuth;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home,container,false);
        post_list = new ArrayList<>();
        post_listview = view.findViewById(R.id.my_post_listView);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        postRecyclerAdapter = new PostRecyclerAdapter(getContext(),post_list);
        post_listview.setAdapter(postRecyclerAdapter);
        post_listview.setLayoutManager(new LinearLayoutManager(getActivity()));

        post_listview.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if(reachedBottom){

                    loadMorePosts();

                }
            }
        });

        Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
                    firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if(documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                post_list.clear();
                            }
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String eventPostId = doc.getDocument().getId();
                                    EventPost eventPost = doc.getDocument().toObject(EventPost.class).withId(eventPostId);

                                    if (isFirstPageLoaded) {

                                        post_list.add(eventPost);

                                    } else {

                                        post_list.add(0, eventPost);

                                    }

                                    postRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        return view;

    }

    public void loadMorePosts() {
        if (mAuth.getCurrentUser() != null) {

            Query nextQuerry = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
                nextQuerry.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String eventPostId = doc.getDocument().getId();
                                    EventPost eventPost = doc.getDocument().toObject(EventPost.class).withId(eventPostId);
                                    post_list.add(eventPost);

                                    postRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }
    }
}
