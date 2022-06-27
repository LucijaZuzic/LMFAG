package com.example.lmfag.utility.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.CreateProfileActivity;
import com.example.lmfag.activities.EditProfileActivity;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.LevelTransformation;

import java.util.List;
import java.util.Locale;

public class CustomAdapterAreaOfInterestRemove extends RecyclerView.Adapter<CustomAdapterAreaOfInterestRemove.ViewHolder> {

    private final List<String> localAreasOfInterest;
    private final List<Double> localLevelPoints;
    private EditProfileActivity editProfileActivity = null;
    private CreateProfileActivity createProfileActivity = null;

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
        String text = localAreasOfInterest.get(position);
        textViewAreaOfInterest.setText(EventTypeToDrawable.getEventTypeToTranslation(textViewAreaOfInterest.getContext(), text));
        textViewAreaOfInterest.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(text), 0, 0, 0);

        double points_level = localLevelPoints.get(position);
        int level = LevelTransformation.level(points_level);
        String text_level = Integer.toString(level);
        double upper_bound = LevelTransformation.upper_bound(level);
        double lower_bound = LevelTransformation.lower_bound(level);
        double range = upper_bound - lower_bound;
        String text_level_points = String.format(Locale.getDefault(), "%.0f / %.0f", points_level, upper_bound).replace(',', '.');
        viewHolder.getDeterminateBar().setProgress((int) ((points_level - lower_bound) / range * 100));
        viewHolder.getTextViewLevel().setText(text_level);
        viewHolder.getTextViewLevelPoints().setText(text_level_points);

        Context ctx1 = editProfileActivity;
        Context ctx2 = createProfileActivity;
        Context ctx = ctx1;
        if (ctx == null) {
            ctx = ctx2;
        }
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
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
        };
        CardView cardView = viewHolder.getCardAreaOfInterest();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        cardView.setOnLongClickListener(view -> {

            builder.setMessage(textViewAreaOfInterest.getContext().getResources().getString(R.string.delete_aoe) + " " + EventTypeToDrawable.getEventTypeToTranslation(textViewAreaOfInterest.getContext(), text) + "?").setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();

            return true;
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
        private final TextView textViewLevel;
        private final TextView textViewLevelPoints;
        private final CardView cardAreaOfInterest;
        private final ProgressBar determinateBar;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewAreaOfInterest = view.findViewById(R.id.textViewAreaOfInterest);
            textViewLevel = view.findViewById(R.id.textViewLevel);
            textViewLevelPoints = view.findViewById(R.id.textViewLevelPoints);
            determinateBar = view.findViewById(R.id.determinateBar);
            cardAreaOfInterest = view.findViewById(R.id.cardAreaOfInterest);
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

        public ProgressBar getDeterminateBar() {
            return determinateBar;
        }

        public CardView getCardAreaOfInterest() {
            return cardAreaOfInterest;
        }
    }
}