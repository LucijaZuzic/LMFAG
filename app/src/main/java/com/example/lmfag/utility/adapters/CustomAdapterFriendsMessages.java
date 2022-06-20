package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterFriendsMessages extends RecyclerView.Adapter<CustomAdapterFriendsMessages.ViewHolder> {

    private final List<String> localFriendUsernames;
    private final SharedPreferences preferences;
    private final Context context;


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapterFriendsMessages(List<String> dataSet, Context context, SharedPreferences preferences) {
        localFriendUsernames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CustomAdapterFriendsMessages.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friends_mesages_list_item, viewGroup, false);

        return new CustomAdapterFriendsMessages.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(localFriendUsernames.get(position));
        viewHolder.getListEntry().setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            String name = localFriendUsernames.get(position);
            editor.putString("friendID", name);
            editor.apply();
            Intent myIntent = new Intent(context, ViewMessagesActivity.class);
            context.startActivity(myIntent);
        });
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    viewHolder.getTextView().setText(Objects.requireNonNull(document.get("username")).toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + localFriendUsernames.get(position));
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        CircleImageView circleImageView = viewHolder.getProfileImage();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(circleImageView.getContext().getApplicationContext());
                        String imageView = preferences.getString("showImage", "true");
                        if (imageView.equals("true")) {
                            Glide.with(circleImageView.getContext().getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView);
                        }
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localFriendUsernames.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewUsername;
        private final CircleImageView profile_image;
        private final LinearLayout listEntry;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textViewUsername = view.findViewById(R.id.textViewUsernameFriend);
            profile_image = view.findViewById(R.id.profile_image_friend);
            listEntry = view.findViewById(R.id.list_entry);
        }

        public TextView getTextView() {
            return textViewUsername;
        }

        public CircleImageView getProfileImage() {
            return profile_image;
        }

        public LinearLayout getListEntry() {
            return listEntry;
        }
    }
}