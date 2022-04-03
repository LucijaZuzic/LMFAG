package com.example.lmfag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterFriendRequest extends RecyclerView.Adapter<CustomAdapterFriendRequest.ViewHolder> {

    private List<String> localFriendUsernames;
    private String receiver;
    private FriendRequests friendRequests;

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

            textViewUsername = (TextView) view.findViewById(R.id.textViewUsernameFriend);
            profile_image = (CircleImageView) view.findViewById(R.id.profile_image_friend);
            accept = (ImageView) view.findViewById(R.id.imageViewApply);
            decline = (ImageView) view.findViewById(R.id.imageViewDiscard);
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

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapterFriendRequest(List<String> dataSet, String receiver, FriendRequests friendRequests) {
        this.receiver = receiver;
        localFriendUsernames = dataSet;
        this.friendRequests = friendRequests;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_request_item, viewGroup, false);

        return new ViewHolder(view);
    }
 
    void addFriendOneDir(String one, String two) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String replace_string = localFriendUsernames.get(position);
        CustomAdapterFriendRequest ca = this;
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(replace_string);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    viewHolder.getTextView().setText(document.get("username").toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + replace_string);
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
        viewHolder.getAccept().setOnClickListener(view -> {
            db.collection("friend_requests")
                .whereEqualTo("sender", replace_string)
                .whereEqualTo("receiver", this.receiver).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task3) {
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
                                        String friends_string = data.get("friends").toString();
                                        if (friends_string.length() > 2) {
                                            String[] friends_string_array = friends_string.substring(1, friends_string.length() - 1).split(", ");
                                            List<String> friends_array = new ArrayList<>();
                                            Collections.addAll(friends_array, friends_string_array);
                                            friends_array.add(replace_string);
                                            Map<String, Object> docuDataNew = document.getData();
                                            docuDataNew.put("friends", friends_array);
                                            db.collection("friends").document(receiver).set(docuDataNew);
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    DocumentSnapshot document2 = task2.getResult();
                                                    if (document2.exists()) {
                                                        Map<String, Object> data2 = document2.getData();
                                                        String friends_string2 = data2.get("friends").toString();
                                                        if (friends_string2.length() > 2) {
                                                            String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            Collections.addAll(friends_array2, friends_string_array2);
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = document2.getData();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db.collection("friends").document(replace_string).set(docuDataNew2);
                                                            Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                            ca.friendRequests.refresh();
                                                        } else {
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = new HashMap<>();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db.collection("friends").document(replace_string).set(docuDataNew2);
                                                            Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                            ca.friendRequests.refresh();
                                                        }
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db.collection("friends").document(replace_string).set(docuDataNew2);
                                                        Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                        ca.friendRequests.refresh();
                                                    }
                                                }
                                            });
                                        } else {
                                            List<String> friends_array = new ArrayList<>();
                                            friends_array.add(replace_string);
                                            Map<String, Object> docuDataNew = new HashMap<>();
                                            docuDataNew.put("friends", friends_array);
                                            db.collection("friends").document(receiver).set(docuDataNew);
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    DocumentSnapshot document2 = task2.getResult();
                                                    if (document2.exists()) {
                                                        Map<String, Object> data2 = document2.getData();
                                                        String friends_string2 = data2.get("friends").toString();
                                                        if (friends_string2.length() > 2) {
                                                            String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            Collections.addAll(friends_array2, friends_string_array2);
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = document2.getData();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db.collection("friends").document(replace_string).set(docuDataNew2);
                                                            Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                            ca.friendRequests.refresh();
                                                        } else {
                                                            List<String> friends_array2 = new ArrayList<>();
                                                            friends_array2.add(receiver);
                                                            Map<String, Object> docuDataNew2 = new HashMap<>();
                                                            docuDataNew2.put("friends", friends_array2);
                                                            db.collection("friends").document(replace_string).set(docuDataNew2);
                                                            Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                            ca.friendRequests.refresh();
                                                        }
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db.collection("friends").document(replace_string).set(docuDataNew2);
                                                        Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                        ca.friendRequests.refresh();
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        List<String> friends_array = new ArrayList<>();
                                        friends_array.add(replace_string);
                                        Map<String, Object> docuDataNew = new HashMap<>();
                                        docuDataNew.put("friends", friends_array);
                                        db.collection("friends").document(receiver).set(docuDataNew);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection("friends").document(replace_string).get().addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();
                                                if (document2.exists()) {
                                                    Map<String, Object> data2 = document2.getData();
                                                    String friends_string2 = data2.get("friends").toString();
                                                    if (friends_string2.length() > 2) {
                                                        String[] friends_string_array2 = friends_string2.substring(1, friends_string2.length() - 1).split(", ");
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        Collections.addAll(friends_array2, friends_string_array2);
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = document2.getData();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db.collection("friends").document(replace_string).set(docuDataNew2);
                                                        Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                        ca.friendRequests.refresh();
                                                    } else {
                                                        List<String> friends_array2 = new ArrayList<>();
                                                        friends_array2.add(receiver);
                                                        Map<String, Object> docuDataNew2 = new HashMap<>();
                                                        docuDataNew2.put("friends", friends_array2);
                                                        db.collection("friends").document(replace_string).set(docuDataNew2);
                                                        Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                        ca.friendRequests.refresh();
                                                    }
                                                } else {
                                                    List<String> friends_array2 = new ArrayList<>();
                                                    friends_array2.add(receiver);
                                                    Map<String, Object> docuDataNew2 = new HashMap<>();
                                                    docuDataNew2.put("friends", friends_array2);
                                                    db.collection("friends").document(replace_string).set(docuDataNew2);
                                                    Snackbar.make(viewHolder.getDecline(), R.string.friend_request_accepted, Snackbar.LENGTH_SHORT).show();
                                                    ca.friendRequests.refresh();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            });
        });
        viewHolder.getDecline().setOnClickListener(view -> {
            db.collection("friend_requests")
                .whereEqualTo("sender", replace_string)
                .whereEqualTo("receiver", this.receiver).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("friend_requests").document(document.getId()).delete();
                            }
                            Snackbar.make(viewHolder.getDecline(), R.string.friend_request_declined, Snackbar.LENGTH_SHORT).show();
                            ca.friendRequests.refresh();
                        }
                    }
                }
            });
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localFriendUsernames.size();
    }
}