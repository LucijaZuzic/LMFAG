package com.example.lmfag.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.activities.CreateEventActivity;
import com.example.lmfag.activities.EditProfileActivity;
import com.example.lmfag.R;
import com.example.lmfag.activities.CreateProfileActivity;
import com.example.lmfag.utility.EventTypeToDrawable;

import java.util.List;

public class CustomAdapterEventTypeAdd extends RecyclerView.Adapter<CustomAdapterEventTypeAdd.ViewHolder> {

    private List<String> localAreasOfInterest;

    private CreateEventActivity createEventActivity = null;

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

            textViewAreaOfInterest = (TextView) view.findViewById(R.id.textViewAreaOfInterest);
            mainLayoutAddAreaOfInterest = (CardView) view.findViewById(R.id.mainLayoutAddAreaOfInterest);
        }

        public TextView getTextViewAreaOfInterest() {
            return textViewAreaOfInterest;
        }

        public CardView getMainLayoutAddAreaOfInterest() { return mainLayoutAddAreaOfInterest; }
    }

    public CustomAdapterEventTypeAdd(List<String> areasOfInterest,  CreateEventActivity createEventActivity) {
        localAreasOfInterest = areasOfInterest;
        this.createEventActivity = createEventActivity;
    }

    // Create new views (invoked by the layout manager)
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
        String text = localAreasOfInterest.get(position);
        viewHolder.getTextViewAreaOfInterest().setText(text);
        viewHolder.getTextViewAreaOfInterest().setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(text), 0, 0, 0);
        viewHolder.getMainLayoutAddAreaOfInterest().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createEventActivity != null) {
                    createEventActivity.selectAreaOfInterest(text);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localAreasOfInterest.size();
    }
}