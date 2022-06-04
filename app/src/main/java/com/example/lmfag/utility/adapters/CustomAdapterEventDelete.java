package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.MyProfileActivity;
import com.example.lmfag.activities.ViewEventActivity;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class CustomAdapterEventDelete extends RecyclerView.Adapter<CustomAdapterEventDelete.ViewHolder> {

    private List<String> localEventNames;
    private SharedPreferences preferences = null;
    private Context context = null;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventNameText;
        private final CardView deleteCard;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            eventNameText = (TextView) view.findViewById(R.id.event_list_entry_text);
            deleteCard = (CardView)  view.findViewById(R.id.event_list_entry);
          }

        public TextView getEventNameText() {
            return eventNameText;
        }
        public CardView getDeleteCard() {
            return deleteCard;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapterEventDelete(List<String> dataSet, Context context, SharedPreferences preferences) {
        localEventNames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomAdapterEventDelete.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_list_item, viewGroup, false);

        return new CustomAdapterEventDelete.ViewHolder(view);
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(localEventNames.get(position));
        String docid = localEventNames.get(position);

        /*viewHolder.getDeleteCard().setOnTouchListener(new MySwipe(context) {
            public void onSwipeTop() {

            }
            public void onSwipeRight() {

                db.collection("events").document(docid).delete();
                db.collection("event_attending").whereEqualTo("event", docid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    db.collection("event_attending").document(document.getId()).delete();
                                }
                            }
                        }
                    }
                });
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("selectedTab", 3);
                editor.apply();
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                context.startActivity(myIntent);
            }
            public void onSwipeLeft() {

                db.collection("events").document(docid).delete();
                db.collection("event_attending").whereEqualTo("event", docid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    db.collection("event_attending").document(document.getId()).delete();
                                }
                            }
                        }
                    }
                });
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("selectedTab", 3);
                editor.apply();
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                context.startActivity(myIntent);
            }
            public void onSwipeBottom() {

            }

        });*/
        viewHolder.getDeleteCard().setOnLongClickListener(view -> {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:


                            db.collection("events").document(docid).delete();
                            db.collection("event_attending").whereEqualTo("event", docid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().size() > 0) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                db.collection("event_attending").document(document.getId()).delete();
                                            }
                                        }
                                    }
                                }
                            });
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("selectedTab", 3);
                            editor.apply();
                            Intent myIntent = new Intent(context, MyProfileActivity.class);
                            context.startActivity(myIntent);

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_event).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();

            return true;
        });
        viewHolder.getDeleteCard().setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            String name = localEventNames.get(position);
            editor.putString("eventID", name);
            editor.apply();
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            context.startActivity(myIntent);
        });
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    TextView et = (TextView) viewHolder.getEventNameText();
                    et.setText(document.get("event_name").toString());
                    et.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(document.get("event_type").toString()), 0, 0, 0);
                    viewHolder.getDeleteCard().setVisibility(View.VISIBLE);
                }
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localEventNames.size();
    }
}