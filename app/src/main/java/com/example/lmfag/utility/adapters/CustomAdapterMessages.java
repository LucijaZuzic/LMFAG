package com.example.lmfag.utility.adapters;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapterMessages extends RecyclerView.Adapter<CustomAdapterMessages.ViewHolder> {

    private List<String> message;
    private List<String> sender;
    private List<String> timestamp;
    private List<String> ids;
    private String me;
    private String myUsername, otherUsername;
    private Bitmap myImage, otherImage;
    private ViewMessagesActivity context;
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

            messageTextView = (TextView) view.findViewById(R.id.textViewLatestMessage);
            time = (TextView) view.findViewById(R.id.textViewTime);
            senderTextView = (TextView) view.findViewById(R.id.textViewSender);
            layout = (LinearLayout) view.findViewById(R.id.list_entry_nested);
            background = (CardView) view.findViewById(R.id.background_change);
            profile_image_two = (CircleImageView) view.findViewById(R.id.profile_image_bubble);
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

        viewHolder.getTime().setText(timestamp.get(position));
        viewHolder.getMessageTextView().setText(message.get(position));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String delete_id = ids.get(position);
        viewHolder.getLayout().setOnClickListener(view -> {
            if (viewHolder.getTime().getVisibility() == View.GONE) {
                viewHolder.getTime().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTime().setVisibility(View.GONE);
            }
        });
        CircleImageView circleImageView = viewHolder.getProfileImageTwo();
        if (!sender.get(position).equals(me)) {
            viewHolder.getSenderTextView().setText(otherUsername);
            if (otherImage != null) {
                circleImageView.setImageBitmap(otherImage);
            }
            viewHolder.getLayout().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            viewHolder.getBackground().setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_teal_700));
            //viewHolder.getSenderTextView().setTextColor(context.getResources().getColor(R.color.white));
            //viewHolder.getMessageTextView().setTextColor(context.getResources().getColor(R.color.white));
            //viewHolder.getTime().setTextColor(context.getResources().getColor(R.color.white));
        } else {
            viewHolder.getSenderTextView().setText(myUsername);
            if (myImage != null) {
                circleImageView.setImageBitmap(myImage);
            }
            viewHolder.getLayout().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            viewHolder.getBackground().setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_700));
            viewHolder.getLayout().setOnLongClickListener(view -> {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                db.collection("messages").document(delete_id).update("messages", R.string.deleted_by_sender);
                                context.getFriendData();
                                context.getAllMessages();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
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
}