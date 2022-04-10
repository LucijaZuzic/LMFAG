package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.RateEvent;
import com.example.lmfag.activities.ViewProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CustomAdapterRating extends RecyclerView.Adapter<CustomAdapterRating.ViewHolder> {

    private List<String> localFriendUsernames;
    private Context context;
    private SharedPreferences preferences;
    private RateEvent rateEvent;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewUsername;
        private final CircleImageView profile_image_player;
        private final RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewUsername = (TextView) view.findViewById(R.id.textViewPlayer);
            profile_image_player = (CircleImageView) view.findViewById(R.id.profile_image_player);
            ratingBar = (RatingBar) view.findViewById(R.id.simpleRatingBarPlayer);
        }

        public TextView getTextView() {
            return textViewUsername;
        }
        public CircleImageView getProfileImage() { return profile_image_player; }
        public RatingBar getRatingBar() {
            return ratingBar;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapterRating(List<String> dataSet, Context context, SharedPreferences preferences, RateEvent rate) {
        localFriendUsernames = dataSet;
        this.context = context;
        this.preferences = preferences;
        this.rateEvent = rate;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rating_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String name = localFriendUsernames.get(position);
        final int rating_position = position;
        DocumentReference docRef = db.collection("users").document(name);
        viewHolder.getRatingBar().setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            // Called when the user swipes the RatingBar
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rateEvent.updateRating(rating_position, viewHolder.getRatingBar().getRating());
            }
        });
        if (!name.equals(preferences.getString("userID", ""))) {
            viewHolder.getProfileImage().setOnClickListener(view -> {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("friendID", name);
                editor.apply();
                Intent myIntent = new Intent(context, ViewProfile.class);
                context.startActivity(myIntent);
            });
        }
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    viewHolder.getTextView().setText(document.get("username").toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = viewHolder.getProfileImage();
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        circleImageView.setImageBitmap(bmp);
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
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
}