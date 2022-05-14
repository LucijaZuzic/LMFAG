package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.activities.EditProfileActivity;
import com.example.lmfag.R;
import com.example.lmfag.activities.CreateProfileActivity;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.MySwipe;

import java.util.List;

public class CustomAdapterAreaOfInterestRemove extends RecyclerView.Adapter<CustomAdapterAreaOfInterestRemove.ViewHolder> {

    private List<String> localAreasOfInterest;
    private List<Double> localLevelPoints;
    private EditProfileActivity editProfileActivity = null;
    private CreateProfileActivity createProfileActivity = null;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAreaOfInterest;
        private final TextView textViewLevel;
        private final TextView textViewLevelPoints;
        //private final ImageView floatingActionButtonRemoveAreaOfInterest;
        private final CardView removableAreaOfInterest;
        private final ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInterest = (TextView) view.findViewById(R.id.textViewAreaOfInterest);
            textViewLevel = (TextView) view.findViewById(R.id.textViewLevel);
            textViewLevelPoints = (TextView) view.findViewById(R.id.textViewLevelPoints);
            // = (ImageView) view.findViewById(R.id.imageViewRemoveAreaOfInterest);
            progressBar = (ProgressBar) view.findViewById(R.id.determinateBar);
            removableAreaOfInterest = (CardView) view.findViewById(R.id.removableAreaOfInterest);
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
        //public ImageView getFloatingActionButtonRemoveAreaOfInterest() { return floatingActionButtonRemoveAreaOfInterest; }
        public ProgressBar getProgressBar() {
            return progressBar;
        }
        public CardView getRemovableAreaOfInterest() { return removableAreaOfInterest; }
    }

    public CustomAdapterAreaOfInterestRemove(List<String> areasOfInterest, List<Double> levelPoints, EditProfileActivity editProfileActivity) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
        this.editProfileActivity = editProfileActivity;
    }
    public CustomAdapterAreaOfInterestRemove(List<String> areasOfInterest, List<Double> levelPoints, CreateProfileActivity createProfileActivity) {
        localAreasOfInterest = areasOfInterest;
        localLevelPoints = levelPoints;
        this.createProfileActivity = createProfileActivity;
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
        /*viewHolder.getFloatingActionButtonRemoveAreaOfInterest().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editProfileActivity != null) {
                    editProfileActivity.removeAreaOfInterest(text);
                }
                if (createProfileActivity != null) {
                    createProfileActivity.removeAreaOfInterest(text);
                }
            }
        });*/
        Context ctx1 = editProfileActivity;
        Context ctx2 = createProfileActivity;
        Context ctx = ctx1;
        if (ctx == null) {
            ctx = ctx2;
       }
        /*viewHolder.getRemovableAreaOfInterest().setOnTouchListener(new MySwipe(ctx) {
            public void onSwipeTop() {

            }
            public void onSwipeRight() {
                if (editProfileActivity != null) {
                    editProfileActivity.removeAreaOfInterest(text);
                }
                if (createProfileActivity != null) {
                    createProfileActivity.removeAreaOfInterest(text);
                }
            }
            public void onSwipeLeft() {
                if (editProfileActivity != null) {
                    editProfileActivity.removeAreaOfInterest(text);
                }
                if (createProfileActivity != null) {
                    createProfileActivity.removeAreaOfInterest(text);
                }
            }
            public void onSwipeBottom() {

            }

        });*/

        Context finalCtx = ctx;
        viewHolder.getRemovableAreaOfInterest().setOnLongClickListener(view -> {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            if (editProfileActivity != null) {
                                editProfileActivity.removeAreaOfInterest(text);
                            }
                            if (createProfileActivity != null) {
                                createProfileActivity.removeAreaOfInterest(text);
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(finalCtx);
            builder.setMessage("Are you sure you want to delete your area of interest " + text + "?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

            return true;
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localAreasOfInterest.size();
    }
}