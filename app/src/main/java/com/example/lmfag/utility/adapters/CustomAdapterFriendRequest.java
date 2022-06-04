package com.example.lmfag.utility.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.activities.FriendRequestsActivity;
import com.example.lmfag.activities.ViewProfileActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterFriendRequest extends RecyclerView.Adapter<CustomAdapterFriendRequest.ViewHolder> {

    private final List<String> localFriendUsernames;
    private final String receiver;
    private final FriendRequestsActivity friendRequestsActivity;

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapterFriendRequest(List<String> dataSet, String receiver, FriendRequestsActivity friendRequestsActivity) {
        this.receiver = receiver;
        localFriendUsernames = dataSet;
        this.friendRequestsActivity = friendRequestsActivity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_request_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String replace_string = localFriendUsernames.get(position);
        CustomAdapterFriendRequest ca = this;
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ImageView decline = viewHolder.getDecline();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(replace_string);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    viewHolder.getTextView().setText(Objects.requireNonNull(document.get("username")).toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + replace_string);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        CircleImageView circleImageView = viewHolder.getProfileImage();
                        circleImageView.setOnClickListener(view -> {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(circleImageView.getContext().getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID",replace_string);
                            editor.apply();
                            Intent intent = new Intent(circleImageView.getContext(), ViewProfileActivity.class);
                            circleImageView.getContext().startActivity(intent);
                        });
                        Glide.with(circleImageView.getContext().getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                }
            }
        });
        viewHolder.getAccept().setOnClickListener(view -> db.collection("friend_requests")
                .whereEqualTo("sender", replace_string)
                .whereEqualTo("receiver", this.receiver).get().addOnCompleteListener(task3 -> {
                    if (task3.isSuccessful()) {
                        if (task3.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document3 : task3.getResult()) {
                                db.collection("friend_requests").document(document3.getId()).delete();
                            }
                            db.collection("friends").document(receiver).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> data = document.getData();
                                        String friends_string = Objects.requireNonNull(Objects.requireNonNull(data).get("friends")).toString();
                                        if (friends_string.length() > 2) {
                                            String[] friends_string_array = friends_string.substring(1, friends_string.length() - 1).split(", ");
                                            List<String> friends_array = new ArrayList<>();
                                            Collections.addAll(friends_array, friends_string_array);
                                            friends_array.add(replace_string);
                                            Map<String, Object> docuDataNew = document.getData();
                                            docuDataNew.put("friends", friends_array);
                                            db.collection("friends").document(receiver).set(docuDataNew);
                                            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                            db1.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    DocumentSnapshot document2 = task2.getResult();
                                                    if (document2.exists()) {
                                                        Map<String, Object> data2 = document2.getData();
                                                        String friends_string2 = Objects.requireNonNull(Objects.requireNonNull(data2).get("friends")).toString();
                                                        if (friends_string2.length() > 2) {
                                                            String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            Collections.addAll(friends_array2, friends_string_array2);
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = document2.getData();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                        } else {
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = new HashMap<>();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                        }
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                    }
                                                    Toast.makeText(decline.getContext().getApplicationContext(), R.string.friend_request_accepted, Toast.LENGTH_SHORT).show();
                                                    ca.friendRequestsActivity.refresh();
                                                }
                                            });
                                        } else {
                                            List<String> friends_array = new ArrayList<>();
                                            friends_array.add(replace_string);
                                            Map<String, Object> docuDataNew = new HashMap<>();
                                            docuDataNew.put("friends", friends_array);
                                            db.collection("friends").document(receiver).set(docuDataNew);
                                            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                            db1.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    DocumentSnapshot document2 = task2.getResult();
                                                    if (document2.exists()) {
                                                        Map<String, Object> data2 = document2.getData();
                                                        String friends_string2 = Objects.requireNonNull(Objects.requireNonNull(data2).get("friends")).toString();
                                                        if (friends_string2.length() > 2) {
                                                            String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            Collections.addAll(friends_array2, friends_string_array2);
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = document2.getData();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                        } else {
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = new HashMap<>();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                        }
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                    }
                                                    Toast.makeText(decline.getContext().getApplicationContext(), R.string.friend_request_accepted, Toast.LENGTH_SHORT).show();
                                                    ca.friendRequestsActivity.refresh();
                                                }
                                            });
                                        }
                                    } else {
                                        List<String> friends_array = new ArrayList<>();
                                        friends_array.add(replace_string);
                                        Map<String, Object> docuDataNew = new HashMap<>();
                                        docuDataNew.put("friends", friends_array);
                                        db.collection("friends").document(receiver).set(docuDataNew);
                                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                                        db1.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();
                                                if (document2.exists()) {
                                                    Map<String, Object> data2 = document2.getData();
                                                    String friends_string2 = Objects.requireNonNull(Objects.requireNonNull(data2).get("friends")).toString();
                                                    if (friends_string2.length() > 2) {
                                                        String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        Collections.addAll(friends_array2, friends_string_array2);
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = document2.getData();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                    }
                                                } else {
                                                    List<String> friends_array2 = new ArrayList<>();
                                                    friends_array2.add(receiver);
                                                    Map<String, Object> docuDataNew2 = new HashMap<>();
                                                    docuDataNew2.put("friends", friends_array2);
                                                    db1.collection("friends").document(replace_string).set(docuDataNew2);
                                                }
                                                Toast.makeText(decline.getContext().getApplicationContext(), R.string.friend_request_accepted, Toast.LENGTH_SHORT).show();
                                                ca.friendRequestsActivity.refresh();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }));
        decline.setOnClickListener(view -> db.collection("friend_requests")
                .whereEqualTo("sender", replace_string)
                .whereEqualTo("receiver", this.receiver).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("friend_requests").document(document.getId()).delete();
                            }
                            Toast.makeText(decline.getContext().getApplicationContext(), R.string.friend_request_declined, Toast.LENGTH_SHORT).show();
                            ca.friendRequestsActivity.refresh();
                        }
                    }
                }));
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
        private final ImageView accept, decline;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewUsername = view.findViewById(R.id.textViewUsernameFriend);
            profile_image = view.findViewById(R.id.profile_image_friend);
            accept = view.findViewById(R.id.imageViewApply);
            decline = view.findViewById(R.id.imageViewDiscard);
        }

        public TextView getTextView() {
            return textViewUsername;
        }

        public CircleImageView getProfileImage() {
            return profile_image;
        }

        public ImageView getAccept() {
            return accept;
        }

        public ImageView getDecline() {
            return decline;
        }
    }
}