package com.example.lmfag;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class CustomAdapterMessages extends RecyclerView.Adapter<CustomAdapterMessages.ViewHolder> {

    private List<String> message;
    private List<String> sender;
    private List<String> timestamp;
    private List<String> ids;
    private String me;
    private ViewMessages context;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final TextView senderTextView;
        private final TextView time;
        private final LinearLayout background;
        private final LinearLayout layout;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            messageTextView = (TextView) view.findViewById(R.id.textViewLatestMessage);
            time = (TextView) view.findViewById(R.id.textViewTime);
            senderTextView = (TextView) view.findViewById(R.id.textViewSender);
            layout = (LinearLayout) view.findViewById(R.id.list_entry_nested);
            background = (LinearLayout) view.findViewById(R.id.background_change);
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
        public LinearLayout getBackground() {
            return background;
        }
    }

    public CustomAdapterMessages(List<String> msg, List<String> times, List<String> senders, List<String> ids, String me, ViewMessages context) {
        message = msg;
        this.ids = ids;
        timestamp = times;
        sender = senders;
        this.me = me;
        this.context = context;
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
        if (!sender.get(position).equals(me)) {
            viewHolder.getLayout().setGravity(Gravity.RIGHT);
            viewHolder.getBackground().setBackground(context.getDrawable(R.drawable.rounded_corner_highlight));
            viewHolder.getBackground().setPadding(5,5,5,5);
            viewHolder.getSenderTextView().setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.getMessageTextView().setTextColor(context.getResources().getColor(R.color.white));
            //viewHolder.getTime().setTextColor(context.getResources().getColor(R.color.white));
        } else {
            viewHolder.getLayout().setOnLongClickListener(view -> {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                db.collection("messages").document(delete_id).update("messages", "Deleted by sender.");
                                context.refresh();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                if (sender.get(position).equals(me)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
                    builder.setMessage("Do you want to delete this message?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
                }
                return true;
            });
        }
        DocumentReference docRef = db.collection("users").document(sender.get(position));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    viewHolder.getSenderTextView().setText(data.get("username").toString());

                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {

                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
        }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return message.size();
    }
}