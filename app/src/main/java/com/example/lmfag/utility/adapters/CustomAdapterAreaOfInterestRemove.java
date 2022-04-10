package com.example.lmfag.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.activities.EditProfile;
import com.example.lmfag.R;
import com.example.lmfag.activities.CreateProfile;
import com.example.lmfag.utility.EventTypeToDrawable;

import java.util.List;

public class CustomAdapterAreaOfInterestRemove extends RecyclerView.Adapter<CustomAdapterAreaOfInterestRemove.ViewHolder> {

    private List<String> localAreasOfInterest;
    private List<Double> localLevelPoints;
    private EditProfile editProfile = null;
    private CreateProfile createProfile = null;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAreaOfInterest;
        private final TextView textViewLevel;
        private final TextView textViewLevelPoints;
        private final ImageView floatingActionButtonRemoveAreaOfInterest;
        private final ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInterest = (TextView) view.findViewById(R.id.textViewAreaOfInterest);
            textViewLevel = (TextView) view.findViewById(R.id.textViewLevel);
            textViewLevelPoints = (TextView) view.findViewById(R.id.textViewLevelPoints);
            floatingActionButtonRemoveAreaOfInterest = (ImageView) view.findViewById(R.id.imageViewRemoveAreaOfInterest);
            progressBar = (ProgressBar) view.findViewById(R.id.determinateBar);
        }

        public TextView getTextViewAreaOfInterest() {
            return textViewAreaOfInterest;
        }
        public TextView getTextViewLevel() {
            return textViewLevel;
        }
        public TextView getTextViewLevelPoints() {
            return textViewLevelPoints;
        }
        public ImageView getFloatingActionButtonRemoveAreaOfInterest() { return floatingActionButtonRemoveAreaOfInterest; }
        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }

    public CustomAdapterAreaOfInterestRemove(List<String> areasOfInterest, List<Double> levelPoints, EditProfile editProfile) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
        this.editProfile = editProfile;
    }
    public CustomAdapterAreaOfInterestRemove(List<String> areasOfInterest, List<Double> levelPoints, CreateProfile createProfile) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
        this.createProfile = createProfile;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.area_of_interest_remove_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextViewAreaOfInterest().setText(localAreasOfInterest.get(position));
        viewHolder.getTextViewAreaOfInterest().setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(localAreasOfInterest.get(position)), 0, 0, 0);
        Integer level = (int) (Math.floor(localLevelPoints.get(position) / 1000));
        String text_level = Integer.toString(level);
        Double upper_bound = Math.ceil(localLevelPoints.get(position)/ 1000) * 1000;
        if (upper_bound.equals(0.0)) {
            upper_bound = 1000.0;
        }
        String text_level_points = localLevelPoints.get(position).toString() + "/" + upper_bound;
        viewHolder.getProgressBar().setProgress((int)((localLevelPoints.get(position) - (upper_bound - 1000)) / 10));
        viewHolder.getTextViewLevel().setText(text_level);
        viewHolder.getTextViewLevelPoints().setText(text_level_points);
        String text = localAreasOfInterest.get(position);
        viewHolder.getFloatingActionButtonRemoveAreaOfInterest().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editProfile != null) {
                    editProfile.removeAreaOfInterest(text);
                }
                if (createProfile != null) {
                    createProfile.removeAreaOfInterest(text);
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