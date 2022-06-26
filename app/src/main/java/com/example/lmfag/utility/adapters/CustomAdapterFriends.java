package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.activities.MyProfileActivity;
import com.example.lmfag.activities.ViewProfileActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterFriends extends RecyclerView.Adapter<CustomAdapterFriends.ViewHolder> {

    private final List<String> localFriendUsernames;
    private final SharedPreferences preferences;
    private final Context context;


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapterFriends(List<String> dataSet, Context context, SharedPreferences preferences) {
        localFriendUsernames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CustomAdapterFriends.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_list_item, viewGroup, false);

        return new CustomAdapterFriends.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CardView listEntry = viewHolder.getListEntry();
        DocumentReference docRef = db.collection("users").document(localFriendUsernames.get(position));
        if (!localFriendUsernames.get(position).equals(preferences.getString("userID", ""))) {
            listEntry.setOnClickListener(view -> {
                SharedPreferences.Editor editor = preferences.edit();
                String name = localFriendUsernames.get(position);
                editor.putString("friendID", name);
                editor.apply();
                Intent myIntent = new Intent(context, ViewProfileActivity.class);
                context.startActivity(myIntent);
            });
        } else {
            listEntry.setOnClickListener(view -> {
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                context.startActivity(myIntent);
            });
        }
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
                        //listEntry.setVisibility(View.VISIBLE);
                    }).addOnFailureListener(exception -> {
                        //listEntry.setVisibility(View.VISIBLE);
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
        private final CardView list_entry;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            list_entry = view.findViewById(R.id.list_entry);
            textViewUsername = view.findViewById(R.id.textViewUsernameFriend);
            profile_image = view.findViewById(R.id.profile_image_friend);
        }

        public TextView getTextView() {
            return textViewUsername;
        }

        public CircleImageView getProfileImage() {
            return profile_image;
        }

        public CardView getListEntry() {
            return list_entry;
        }
    }
}