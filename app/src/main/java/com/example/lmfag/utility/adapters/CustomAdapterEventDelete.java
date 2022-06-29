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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Objects;

public class CustomAdapterEventDelete extends RecyclerView.Adapter<CustomAdapterEventDelete.ViewHolder> {

    private final List<String> localEventNames;
    private final SharedPreferences preferences;
    private final Context context;

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapterEventDelete(List<String> dataSet, Context context, SharedPreferences preferences) {
        localEventNames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
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
        String docID = localEventNames.get(position);

        CardView deleteCard = viewHolder.getDeleteCard();

        deleteCard.setOnLongClickListener(view -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:


                        db.collection("events").document(docID).delete();
                        db.collection("event_attending").whereEqualTo("event", docID).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() > 0) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        db.collection("event_attending").document(document.getId()).delete();
                                    }
                                }
                            }
                        });
                        Intent myIntent = new Intent(context, MyProfileActivity.class);
                        myIntent.putExtra("selectedTab", 3);
                        context.startActivity(myIntent);

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_event).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();

            return true;
        });
        deleteCard.setOnClickListener(view -> {
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
                    TextView et = viewHolder.getEventNameText();
                    et.setText(Objects.requireNonNull(document.get("event_name")).toString());
                    et.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(document.get("event_type")).toString()), 0, 0, 0);
                    deleteCard.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localEventNames.size();
    }

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

            eventNameText = view.findViewById(R.id.event_list_entry_text);
            deleteCard = view.findViewById(R.id.event_list_entry);
        }

        public TextView getEventNameText() {
            return eventNameText;
        }

        public CardView getDeleteCard() {
            return deleteCard;
        }
    }
}