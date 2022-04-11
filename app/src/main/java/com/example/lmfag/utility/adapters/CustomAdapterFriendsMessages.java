package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterFriendsMessages extends RecyclerView.Adapter<CustomAdapterFriendsMessages.ViewHolder> {

    private List<String> localFriendUsernames;
    private SharedPreferences preferences = null;
    private Context context = null;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewUsername;
        private final CircleImageView profile_image;
        private final CircleImageView profile_image_two;
        private TextView latestMessage;
        private TextView sender;
        private TextView time;
        private LinearLayout nested;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewUsername = (TextView) view.findViewById(R.id.textViewUsernameFriend);
            profile_image = (CircleImageView) view.findViewById(R.id.profile_image_friend);
            profile_image_two = (CircleImageView) view.findViewById(R.id.profile_image_bubble);
            latestMessage = (TextView) view.findViewById(R.id.textViewLatestMessage);
            sender = (TextView) view.findViewById(R.id.textViewSender);
            nested = (LinearLayout) view.findViewById(R.id.list_entry_nested);
            time = (TextView) view.findViewById(R.id.textViewTime);
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
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapterFriendsMessages(List<String> dataSet, Context context, SharedPreferences preferences) {
        localFriendUsernames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomAdapterFriendsMessages.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friends_mesages_list_item, viewGroup, false);

        return new CustomAdapterFriendsMessages.ViewHolder(view);
    }
    // Replace the contents of a view (invoked by the layout manager)

    private void getOrganizerData(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(localFriendUsernames.get(position));
        viewHolder.getProfileImage().setOnClickListener(view -> {
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
                    viewHolder.getTextView().setText(document.get("username").toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + localFriendUsernames.get(position));
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
        final String other = localFriendUsernames.get(position);
        viewHolder.getNested().setOnClickListener(view -> {
            if (viewHolder.getTime().getVisibility() == View.GONE) {
                viewHolder.getTime().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTime().setVisibility(View.GONE);
            }
        });
        db.collection("messages")
                .whereIn("receiver", Arrays.asList(preferences.getString("userID",""),other))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int number_entry = 0;
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (number_entry > 0) {
                                continue;
                            }
                            if (document.get("sender").toString().equals(preferences.getString("userID", "")) || document.get("sender").toString().equals(other)) {
                                number_entry = 1;
                                viewHolder.getLatest().setText(document.getData().get("messages").toString());
                                Timestamp start_timestamp = (Timestamp) (document.getData().get("timestamp"));
                                Date start_date = start_timestamp.toDate();
                                Calendar cldr_start = Calendar.getInstance();
                                cldr_start.setTime(start_date);
                                viewHolder.getTime().setText(DateFormat.getDateTimeInstance().format(cldr_start.getTime()));
                                DocumentReference docRef = db.collection("users").document(document.getData().get("sender").toString());
                                docRef.get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document2 = task2.getResult();
                                        if (document2.exists()) {
                                            Map<String, Object> data = document2.getData();
                                            viewHolder.getSender().setText(data.get("username").toString());
                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                            StorageReference storageRef = storage.getReference();
                                            StorageReference imagesRef = storageRef.child("profile_pictures/" + document2.getId());
                                            final long ONE_MEGABYTE = 1024 * 1024;
                                            imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                                                // Data for "images/island.jpg" is returns, use this as needed
                                                CircleImageView circleImageView = viewHolder.getProfileImageTwo();
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
                        }
                    }
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