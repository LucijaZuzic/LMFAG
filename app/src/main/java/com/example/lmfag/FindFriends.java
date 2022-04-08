package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindFriends extends MenuInterface {
    Context context = this;
    RecyclerView recyclerViewFindFriends;
    SharedPreferences preferences;
    Spinner search_params;
    Spinner sort_params;
    EditText editTextSearchValue;
    ImageView imageViewBeginSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        DrawerHelper.fillNavbarData(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewFindFriends = findViewById(R.id.recyclerViewFriends);
        imageViewBeginSearch = findViewById(R.id.imageViewBeginSearch);
        editTextSearchValue = findViewById(R.id.editTextSearchValue);
        imageViewBeginSearch.setOnClickListener(view -> {
            getAllFriends();
        });
    }

    void fillSpinner() {
        ArrayAdapter<CharSequence> adapter_search_params = ArrayAdapter.createFromResource(this, R.array.event_search_params, android.R.layout.simple_spinner_item);
        adapter_search_params.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        search_params.setAdapter(adapter_search_params);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_sort_params, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_params.setAdapter(adapter);
    }

    void getAllFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> friends_array = new ArrayList<>();
        Query q = db.collection("users").orderBy("username");
        String text = editTextSearchValue.getText().toString();
        if (!text.equals("")) {
            q = db.collection("users").whereEqualTo("username", text);
        }
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(!(document.getId().equals(preferences.getString("userID", "")))) {
                                friends_array.add(document.getId());
                            }
                        }
                        CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array, context, preferences);
                        recyclerViewFindFriends.setAdapter(customAdapterFriends);
                    } else {
                        CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(new ArrayList<String>(), context, preferences);
                        recyclerViewFindFriends.setAdapter(customAdapterFriends);
                    }
                } else {
                    CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(new ArrayList<String>(), context, preferences);
                    recyclerViewFindFriends.setAdapter(customAdapterFriends);
                }
            }
        });
    }


}