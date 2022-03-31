package com.example.lmfag;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;

public class CustomAdapterEditAreaOfInterest extends RecyclerView.Adapter<CustomAdapterEditAreaOfInterest.ViewHolder> {

    private String[] localAreasOfInterest;
    private Double[] localLevelPoints;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAreaOfInteres;
        private final TextView textViewLevel;
        private final TextView textViewLevelPoints;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInteres = (TextView) view.findViewById(R.id.textViewAreaOfInterest);
            textViewLevel = (TextView) view.findViewById(R.id.textViewLevel);
            textViewLevelPoints = (TextView) view.findViewById(R.id.textViewLevelPoints);
        }

        public TextView getTextViewAreaOfInteres() {
            return textViewAreaOfInteres;
        }
        public TextView getTextViewLevel() {
            return textViewLevel;
        }
        public TextView getTextViewLevelPoints() {
            return textViewLevelPoints;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomAdapterEditAreaOfInterest(String[] areasOfInterest, Double[] levelPoints) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.edit_area_of_interest_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextViewAreaOfInteres().setText(localAreasOfInterest[position]);
        Integer level = (int) (Math.floor(localLevelPoints[position] / 1000));
        String text_level = Integer.toString(level);
        Double upper_bound = Math.ceil(localLevelPoints[position] / 1000) * 1000;
        String text_level_points = localLevelPoints[position] + "/" + upper_bound;
        viewHolder.getTextViewLevel().setText(text_level);
        viewHolder.getTextViewLevelPoints().setText(text_level_points);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localAreasOfInterest.length;
    }
}