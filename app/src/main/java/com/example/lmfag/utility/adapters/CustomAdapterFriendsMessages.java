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
        TextView time = viewHolder.getTime();
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
        final String other = localFriendUsernames.get(position);
        viewHolder.getNested().setOnClickListener(view -> {
            if (time.getVisibility() == View.GONE) {
                time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }
        });
        db.collection("messages")
                .whereIn("receiver", Arrays.asList(preferences.getString("userID", ""), other))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    int number_entry = 0;
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (number_entry > 0) {
                                    continue;
                                }
                                if (Objects.requireNonNull(document.get("sender")).toString().equals(preferences.getString("userID", "")) || Objects.requireNonNull(document.get("sender")).toString().equals(other)) {
                                    number_entry = 1;
                                    viewHolder.getLatest().setText(Objects.requireNonNull(document.getData().get("messages")).toString());
                                    Timestamp start_timestamp = (Timestamp) (document.getData().get("timestamp"));
                                    assert start_timestamp != null;
                                    Date start_date = start_timestamp.toDate();
                                    Calendar cldr_start = Calendar.getInstance();
                                    cldr_start.setTime(start_date);
                                    time.setText(DateFormat.getDateTimeInstance().format(cldr_start.getTime()));
                                    DocumentReference docRef1 = db.collection("users").document(Objects.requireNonNull(document.getData().get("sender")).toString());
                                    docRef1.get().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot document2 = task2.getResult();
                                            if (document2.exists()) {
                                                Map<String, Object> data = document2.getData();
                                                viewHolder.getSender().setText(Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());
                                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                                StorageReference storageRef = storage.getReference();
                                                StorageReference imagesRef = storageRef.child("profile_pictures/" + document2.getId());
                                                final long ONE_MEGABYTE = 1024 * 1024;
                                                imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                                                    CircleImageView circleImageView = viewHolder.getProfileImageTwo();
                                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(circleImageView.getContext().getApplicationContext());
                                                    String imageView = preferences.getString("showImage", "true");
                                                    if (imageView.equals("true")) {
                                                        Glide.with(circleImageView.getContext().getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView);
                                                    }                                                }).addOnFailureListener(exception -> {
                                                    // Handle any errors
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
    }
    // Replace the contents of a view (invoked by the layout manager)

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
        private final CircleImageView profile_image_two;
        private final TextView latestMessage;
        private final TextView sender;
        private final TextView time;
        private final LinearLayout nested;
        private final CardView listEntry;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewUsername = view.findViewById(R.id.textViewUsernameFriend);
            profile_image = view.findViewById(R.id.profile_image_friend);
            profile_image_two = view.findViewById(R.id.profile_image_bubble);
            latestMessage = view.findViewById(R.id.textViewLatestMessage);
            sender = view.findViewById(R.id.textViewSender);
            nested = view.findViewById(R.id.list_entry_nested);
            time = view.findViewById(R.id.textViewTime);
            listEntry = view.findViewById(R.id.list_entry);
        }

        public TextView getTextView() {
            return textViewUsername;
        }

        public CircleImageView getProfileImage() {
            return profile_image;
        }

        public CircleImageView getProfileImageTwo() {
            return profile_image_two;
        }

        public TextView getLatest() {
            return latestMessage;
        }

        public TextView getTime() {
            return time;
        }

        public TextView getSender() {
            return sender;
        }

        public LinearLayout getNested() {
            return nested;
        }

        public CardView getListEntry() {
            return listEntry;
        }
    }
}