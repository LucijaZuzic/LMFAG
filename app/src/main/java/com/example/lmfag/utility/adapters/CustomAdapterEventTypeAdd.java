package com.example.lmfag.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.CreateEventActivity;
import com.example.lmfag.activities.FindEventsActivity;
import com.example.lmfag.utility.EventTypeToDrawable;

import java.util.List;

public class CustomAdapterEventTypeAdd extends RecyclerView.Adapter<CustomAdapterEventTypeAdd.ViewHolder> {

    private final List<String> localAreasOfInterest;

    private CreateEventActivity createEventActivity = null;
    private FindEventsActivity findEventsActivity = null;

    public CustomAdapterEventTypeAdd(List<String> areasOfInterest, CreateEventActivity createEventActivity) {
        localAreasOfInterest = areasOfInterest;
        this.createEventActivity = createEventActivity;
    }

    public CustomAdapterEventTypeAdd(List<String> areasOfInterest, FindEventsActivity findEventsActivity) {
        localAreasOfInterest = areasOfInterest;
        this.findEventsActivity = findEventsActivity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.area_of_interest_add_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        TextView textViewAreaOfInterest = viewHolder.getTextViewAreaOfInterest();
        String text = localAreasOfInterest.get(position);
        textViewAreaOfInterest.setText(EventTypeToDrawable.getEventTypeToTranslation(textViewAreaOfInterest.getContext(), text));
        textViewAreaOfInterest.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(text), 0, 0, 0);
        viewHolder.getMainLayoutAddAreaOfInterest().setOnClickListener(view -> {
            if (createEventActivity != null) {
                createEventActivity.selectAreaOfInterest(text);
            }
            if (findEventsActivity != null) {
                findEventsActivity.selectAreaOfInterest(text);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localAreasOfInterest.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAreaOfInterest;
        private final CardView mainLayoutAddAreaOfInterest;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInterest = view.findViewById(R.id.textViewAreaOfInterest);
            mainLayoutAddAreaOfInterest = view.findViewById(R.id.mainLayoutAddAreaOfInterest);
        }

        public TextView getTextViewAreaOfInterest() {
            return textViewAreaOfInterest;
        }

        public CardView getMainLayoutAddAreaOfInterest() {
            return mainLayoutAddAreaOfInterest;
        }
    }
}