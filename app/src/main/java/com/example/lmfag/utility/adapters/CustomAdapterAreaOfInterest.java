package com.example.lmfag.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;

import java.util.List;

public class CustomAdapterAreaOfInterest extends RecyclerView.Adapter<CustomAdapterAreaOfInterest.ViewHolder> {

    private final List<String> localAreasOfInterest;
    private final List<Double> localLevelPoints;

    public CustomAdapterAreaOfInterest(List<String> areasOfInterest, List<Double> levelPoints) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.area_of_interest_list_item, viewGroup, false);

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
        int level = (int) (Math.floor(localLevelPoints.get(position) / 1000));
        String text_level = Integer.toString(level);
        Double upper_bound = Math.ceil(localLevelPoints.get(position) / 1000) * 1000;
        if (upper_bound.equals(0.0)) {
            upper_bound = 1000.0;
        }
        String text_level_points = localLevelPoints.get(position).toString() + "/" + upper_bound;
        viewHolder.getProgressBar().setProgress((int) ((localLevelPoints.get(position) - (upper_bound - 1000)) / 10));
        viewHolder.getTextViewLevel().setText(text_level);
        viewHolder.getTextViewLevelPoints().setText(text_level_points);
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
        private final TextView textViewLevel;
        private final TextView textViewLevelPoints;
        private final ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInterest = view.findViewById(R.id.textViewAreaOfInterest);
            textViewLevel = view.findViewById(R.id.textViewLevel);
            textViewLevelPoints = view.findViewById(R.id.textViewLevelPoints);
            progressBar = view.findViewById(R.id.determinateBar);
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

        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }
}