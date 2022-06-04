package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.ViewEventActivity;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class CustomAdapterEvent extends RecyclerView.Adapter<CustomAdapterEvent.ViewHolder> {

    private final List<String> localEventNames;
    private final SharedPreferences preferences;
    private final Context context;

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapterEvent(List<String> dataSet, Context context, SharedPreferences preferences) {
        localEventNames = dataSet;
        this.preferences = preferences;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CustomAdapterEvent.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_list_item, viewGroup, false);

        return new CustomAdapterEvent.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(localEventNames.get(position));
        CardView cardView = viewHolder.getCardView();
        cardView.setOnClickListener(view -> {
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
                    TextView et = viewHolder.getTextView();
                    et.setText(Objects.requireNonNull(document.get("event_name")).toString());
                    et.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(document.get("event_type")).toString()), 0, 0, 0);
                    // cardView.setVisibility(View.VISIBLE);
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
        private final CardView cardViewEventItem;
        private final TextView cardViewEventText;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            cardViewEventItem = view.findViewById(R.id.event_list_entry);
            cardViewEventText = view.findViewById(R.id.event_list_entry_text);
        }

        public CardView getCardView() {
            return cardViewEventItem;
        }

        public TextView getTextView() {
            return cardViewEventText;
        }
    }
}