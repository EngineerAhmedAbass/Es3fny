package com.es3fny.Request;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;



/**
 * Created by ahmed on 27-Mar-18.
 */

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    private List<MyNotification> notificationsList;
    private Context context;
    private FirebaseFirestore db;

    public NotificationsRecyclerAdapter(Context context, List<MyNotification> notificationsList) {
        this.notificationsList = notificationsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.e("OnBindViewHolder","hereeeeeeeeeeeeeeeee");
        holder.user_name_view.setText(notificationsList.get(position).getUser_name());
        holder.Domain_view.setText(notificationsList.get(position).getDomain());
        String Distance_Text = String.format("%.2f", notificationsList.get(position).getDistance())+context.getString(R.string.km);
        holder.Distance.setText(Distance_Text);
        final String Notification_Id = notificationsList.get(position).notificationId;
        final String Current_User_Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Users").document(Current_User_Id).collection("Notifications").document(Notification_Id);

        holder.mview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(context.getString(R.string.loading));
                progressDialog.setMessage(context.getString(R.string.wait_till_notification_loads));
                progressDialog.show();
                    if (isNetworkAvailable()) {
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                MyNotification notification = documentSnapshot.toObject(MyNotification.class);
                                GoToNotifications(notification);
                                progressDialog.hide();
                            }
                        });
                    } else {
                        progressDialog.hide();
                        Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
            }
        });
        holder.mview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to Delete This Notification?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (isNetworkAvailable()) {
                                    DeleteNotification(Notification_Id,Current_User_Id);
                                } else {
                                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

    private void DeleteNotification(String notification_id,String User_id) {
        db.collection("Users").document(User_id).collection("Notifications").document(notification_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, R.string.notification_deleted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void GoToNotifications(MyNotification notification) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("message", notification.getMessage());
        intent.putExtra("from_name", notification.getUser_name());
        intent.putExtra("from_id",notification.getFrom());
        intent.putExtra("latitude", notification.getLatitude());
        intent.putExtra("longtitude", notification.getLongtitude());
        intent.putExtra("domain", notification.getDomain());
        intent.putExtra("request_id", notification.getRequestID());
        intent.putExtra("type", notification.getType());
        context.startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mview;

        private TextView user_name_view, Domain_view , Distance;

        public ViewHolder(View itemView) {
            super(itemView);

            mview = itemView;

            user_name_view = mview.findViewById(R.id.sender_name);
            Domain_view =  mview.findViewById(R.id.domain);
            Distance = mview.findViewById(R.id.distance);
        }
    }

}
