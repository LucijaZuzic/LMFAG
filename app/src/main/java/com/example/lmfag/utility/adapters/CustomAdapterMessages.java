package com.example.lmfag.utility.adapters;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterMessages extends RecyclerView.Adapter<CustomAdapterMessages.ViewHolder> {

    private final List<String> message;
    private final List<String> sender;
    private final List<String> timestamp;
    private final List<String> ids;
    private final String me;
    private final String myUsername;
    private final String otherUsername;
    private final Bitmap myImage;
    private final Bitmap otherImage;
    private final ViewMessagesActivity context;

    public CustomAdapterMessages(List<String> msg, List<String> times, List<String> senders, List<String> ids, String me, ViewMessagesActivity context, String myUsername, String otherUsername, Bitmap myImage, Bitmap otherImage) {
        message = msg;
        this.ids = ids;
        timestamp = times;
        sender = senders;
        this.me = me;
        this.context = context;
        this.myUsername = myUsername;
        this.myImage = myImage;
        this.otherUsername = otherUsername;
        this.otherImage = otherImage;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mesages_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        TextView time = viewHolder.getTime();
        LinearLayout layout = viewHolder.getLayout();
        TextView senderTextView = viewHolder.getSenderTextView();
        TextView messageTextView = viewHolder.getMessageTextView();
        CardView background = viewHolder.getBackground();
        time.setText(timestamp.get(position));
        messageTextView.setText(message.get(position));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String delete_id = ids.get(position);
        layout.setOnClickListener(view -> {
            if (time.getVisibility() == View.GONE) {
                time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }
        });
        CircleImageView circleImageView = viewHolder.getProfileImageTwo();
        if (!sender.get(position).equals(me)) {
            senderTextView.setText(otherUsername);
            if (otherImage != null) {
                circleImageView.setImageBitmap(otherImage);
            }
            layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            background.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_teal_700));
            // Old version set to white senderTextView.setTextColor(context.getResources().getColor(R.color.white));
            // Old version set to white messageTextView.setTextColor(context.getResources().getColor(R.color.white));
            //time.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            senderTextView.setText(myUsername);
            if (myImage != null) {
                circleImageView.setImageBitmap(myImage);
            }
            layout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            background.setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_700));
            layout.setOnLongClickListener(view -> {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            db.collection("messages").document(delete_id).update("messages", context.getResources().getString(R.string.deleted_by_sender));
                            context.getAllMessages();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
                builder.setMessage(R.string.delete_message).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
                return true;
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return message.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final TextView senderTextView;
        private final TextView time;
        private final CardView background;
        private final LinearLayout layout;
        private final CircleImageView profile_image_two;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            messageTextView = view.findViewById(R.id.textViewLatestMessage);
            time = view.findViewById(R.id.textViewTime);
            senderTextView = view.findViewById(R.id.textViewSender);
            layout = view.findViewById(R.id.list_entry_nested);
            background = view.findViewById(R.id.background_change);
            profile_image_two = view.findViewById(R.id.profile_image_bubble);
        }

        public TextView getMessageTextView() {
            return messageTextView;
        }

        public TextView getSenderTextView() {
            return senderTextView;
        }

        public TextView getTime() {
            return time;
        }

        public LinearLayout getLayout() {
            return layout;
        }

        public CardView getBackground() {
            return background;
        }

        public CircleImageView getProfileImageTwo() {
            return profile_image_two;
        }
    }
}