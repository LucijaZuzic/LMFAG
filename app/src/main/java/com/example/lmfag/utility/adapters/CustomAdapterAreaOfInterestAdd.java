package com.example.lmfag.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.CreateProfileActivity;
import com.example.lmfag.activities.EditProfileActivity;
import com.example.lmfag.utility.EventTypeToDrawable;

import java.util.List;

public class CustomAdapterAreaOfInterestAdd extends RecyclerView.Adapter<CustomAdapterAreaOfInterestAdd.ViewHolder> {

    private final List<String> localAreasOfInterest;

    private EditProfileActivity editProfileActivity = null;
    private CreateProfileActivity createProfileActivity = null;

    public CustomAdapterAreaOfInterestAdd(List<String> areasOfInterest, EditProfileActivity editProfileActivity) {
        localAreasOfInterest = areasOfInterest;
        this.editProfileActivity = editProfileActivity;
    }

    public CustomAdapterAreaOfInterestAdd(List<String> areasOfInterest, CreateProfileActivity createProfileActivity) {
        localAreasOfInterest = areasOfInterest;
        this.createProfileActivity = createProfileActivity;
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
        textViewAreaOfInterest.setText(localAreasOfInterest.get(position));
        textViewAreaOfInterest.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(localAreasOfInterest.get(position)), 0, 0, 0);
        String text = localAreasOfInterest.get(position);
        viewHolder.getMainLayoutAddAreaOfInterest().setOnClickListener(view -> {
            if (editProfileActivity != null) {
                editProfileActivity.addAreaOfInterest(text);
            }
            if (createProfileActivity != null) {
                createProfileActivity.addAreaOfInterest(text);
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