package com.example.lmfag.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsActivity extends MenuInterfaceActivity {
    private Context context;
    private RecyclerView recyclerViewFindFriends;
    private EditText editTextSearchValue;
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        context = this;
        
        noResults = findViewById(R.id.noResults);
        recyclerViewFindFriends = findViewById(R.id.recyclerViewFriends);
        ImageView imageViewBeginSearch = findViewById(R.id.imageViewBeginSearch);
        editTextSearchValue = findViewById(R.id.editTextSearchValue);
        imageViewBeginSearch.setOnClickListener(view -> getAllFriends());
    }

    private void getAllFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> friends_array = new ArrayList<>();
        Query q = db.collection("users").orderBy("username");
        String text = editTextSearchValue.getText().toString();
        if (!text.equals("")) {
            q = db.collection("users").whereEqualTo("username", text);
        }
        q.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (!(document.getId().equals(preferences.getString("userID", "")))) {
                            friends_array.add(document.getId());
                        }
                    }
                    CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array, context, preferences);
                    recyclerViewFindFriends.setAdapter(customAdapterFriends);
                    if (friends_array.size() > 0) {
                        noResults.setVisibility(View.GONE);
                    } else {
                        noResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(new ArrayList<>(), context, preferences);
                    recyclerViewFindFriends.setAdapter(customAdapterFriends);
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(new ArrayList<>(), context, preferences);
                recyclerViewFindFriends.setAdapter(customAdapterFriends);
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }
}