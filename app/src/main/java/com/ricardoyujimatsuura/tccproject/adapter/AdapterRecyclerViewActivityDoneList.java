package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.activity.ActivityDoneActivity;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.enums.ValueTypes;
import com.ricardoyujimatsuura.tccproject.models.ActivityDone;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterRecyclerViewActivityDoneList extends RecyclerView.Adapter<AdapterRecyclerViewActivityDoneList.MyViewHolder>{

    ArrayList<ActivityDone> list;
    Context context;
    FirebaseAuth auth;
    private String userId;

    public static final String MODE = "MODE";
    public static final String EDIT_ACTIVITY_DONE = "EDIT_ACTIVITY_DONE";
    public static final int NEW = 1;
    public static final int EDIT = 2;

    public AdapterRecyclerViewActivityDoneList(Context context, ArrayList<ActivityDone> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterRecyclerViewActivityDoneList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_activity_done_list, parent, false);
        auth = FirebaseConfig.getFirebaseAuth();
        userId = auth.getCurrentUser().getUid();
        return new AdapterRecyclerViewActivityDoneList.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecyclerViewActivityDoneList.MyViewHolder holder, int position) {

        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();

        ActivityDone activityDone = list.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(activityDone.getTimestamp());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String dateTime = String.format(Locale.getDefault(), context.getString(R.string.day_month_year_hour_minute_variables), day, month, year, hour, minute);

        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                    activityType.setValueType(ValueTypes.valueOf(childSnapshot.child("valueType").getValue(String.class)));
                    if(activityDone.getActivityTypeAuxId().equals(childSnapshot.getKey())) {
                        if(activityType.getValueType().equals(ValueTypes.MINUTES)) {
                            String convertedValue = convertToTime(activityDone.getValue());
                            holder.value.setText(convertedValue);
                        }
                        else {
                            holder.value.setText(String.valueOf(activityDone.getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //holder.value.setText(String.valueOf(activityDone.getValue()));
        holder.date.setText(dateTime);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.delete_the_activity_done);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activityDone.deleteFromFirebase();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityDoneActivity.class);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_ACTIVITY_DONE, activityDone);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView value, date;
        Button delete, edit;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            value = itemView.findViewById(R.id.textViewRecyclerActivityDoneValue);
            date = itemView.findViewById(R.id.textViewRecyclerActivityDoneDate);
            edit = itemView.findViewById(R.id.materialButtonEditActivityDone);
            delete = itemView.findViewById(R.id.materialButtonDeleteActivityDone);

        }
    }

    public String convertToTime(double value) {
        int totalMinutes = (int) value;
        int hour = totalMinutes/60;
        int minutes = totalMinutes % 60;
        return hour + context.getString(R.string.h_hours) + minutes + context.getString(R.string.m_minutes);
    }

}
