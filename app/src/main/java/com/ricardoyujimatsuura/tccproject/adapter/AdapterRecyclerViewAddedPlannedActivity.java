package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.activity.ActivityDoneActivity;
import com.ricardoyujimatsuura.tccproject.activity.AddPlannedActivity;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.enums.StatisticsType;
import com.ricardoyujimatsuura.tccproject.enums.ValueTypes;
import com.ricardoyujimatsuura.tccproject.models.ActivityDone;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;
import com.ricardoyujimatsuura.tccproject.models.PlannedActivity;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdapterRecyclerViewAddedPlannedActivity extends RecyclerView.Adapter<AdapterRecyclerViewAddedPlannedActivity.MyViewHolder> {

    ArrayList<PlannedActivity> list;
    Context context;
    FirebaseAuth auth;
    private String userId;
    //private Double value;
    private Double percentage;

    public static final String MODE = "MODE";
    public static final String EDIT_PLANNED_ACTIVITY = "EDIT_PLANNED_ACTIVITY";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    public static final int VIEW = 3;

    ActivityResultLauncher<Intent> mGetContent;

    public AdapterRecyclerViewAddedPlannedActivity(ArrayList<PlannedActivity> list, Context context, ActivityResultLauncher<Intent> mGetContent) {
        this.list = list;
        this.context = context;
        this.mGetContent = mGetContent;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_added_planned_activity, parent, false);
        auth = FirebaseConfig.getFirebaseAuth();
        userId = auth.getCurrentUser().getUid();

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        int currentPosition = holder.getAdapterPosition();
        PlannedActivity plannedActivity = list.get(position);

        //query para setar a meta, dependendo do tipo de valor dela
        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                    activityType.setValueType(ValueTypes.valueOf(childSnapshot.child("valueType").getValue(String.class)));
                    if(plannedActivity.getActivityTypeId().equals(childSnapshot.getKey())) {
                        if(activityType.getValueType().equals(ValueTypes.MINUTES)) {
                            String stringGoalValue = convertToTime(plannedActivity.getGoal());
                            holder.goal.setText(stringGoalValue);
                        }
                        else {
                            holder.goal.setText(String.valueOf(plannedActivity.getGoal()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.name.setText(plannedActivity.getDescription());
        //holder.goal.setText(String.valueOf(plannedActivity.getGoal()));
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Html.fromHtml(context.getString(R.string.delete_the_planned_activity_bold)
                        + plannedActivity.getDescription()
                        + context.getString(R.string.activities_done_related)));
                builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(plannedActivity.getPushId() != null) {
                            DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
                            firebase.child("activityDone").child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        ActivityDone activityDone;
                                        activityDone = childSnapshot.getValue(ActivityDone.class);
                                        activityDone.setPushId(childSnapshot.getKey());
                                        activityDone.setUserId(userId);
                                        if (plannedActivity.getPushId().equals(activityDone.getPlannedActivityId())) {
                                            activityDone.deleteFromFirebase();
                                        }
                                    }
                                    plannedActivity.deleteFromFirebase();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        list.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, list.size());
                        Toast.makeText(context, plannedActivity.getDescription() + context.getString(R.string.deleted_planned_activity), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.create();
                builder.show();
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddPlannedActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) list);
                intent.putExtra("BUNDLE", args);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_PLANNED_ACTIVITY, plannedActivity);
                mGetContent.launch(intent);
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddPlannedActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) list);
                intent.putExtra("BUNDLE", args);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_PLANNED_ACTIVITY, plannedActivity);
                mGetContent.launch(intent);
            }
        });

        if(plannedActivity.getPushId() != null) {
            holder.addActivityDone.setVisibility(View.VISIBLE);
            //DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
            firebase.child("activityDone").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double valueOfAllActivities = 0.0;
                    int countAverage = 0;
                    for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                        ActivityDone activityDone = childSnapshot.getValue(ActivityDone.class);
                        if(plannedActivity.getPushId().equals(activityDone.getPlannedActivityId())) {
                            valueOfAllActivities += activityDone.getValue();
                            countAverage++;
                        }
                    }
                    if(valueOfAllActivities > 0) {

                        percentage = 0.0;
                        holder.activitiesDone.setVisibility(View.VISIBLE);
                        holder.percentageDone.setVisibility(View.VISIBLE);
                        holder.activitiesDoneFix.setVisibility(View.VISIBLE);
                        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
                        double finalValueOfAllActivities = valueOfAllActivities;
                        int finalCountAverage = countAverage;
                        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                                    activityType.setValueType(ValueTypes.valueOf(childSnapshot.child("valueType").getValue(String.class)));
                                    activityType.setStatisticsType(StatisticsType.valueOf(childSnapshot.child("statisticsType").getValue(String.class)));
                                    if(plannedActivity.getActivityTypeId().equals(childSnapshot.getKey())) {
                                        if(activityType.getStatisticsType().equals(StatisticsType.SUM)) {
                                            if(activityType.getValueType().equals(ValueTypes.MINUTES)){
                                                String stringSumMsg = convertToTime(finalValueOfAllActivities);
                                                holder.activitiesDone.setText(stringSumMsg);
                                            }
                                            else {
                                                DecimalFormat numberFormat = new DecimalFormat("#0.00");
                                                String activityDoneSum = numberFormat.format(finalValueOfAllActivities);
                                                holder.activitiesDone.setText(activityDoneSum);
                                            }
                                            DecimalFormat numberFormat = new DecimalFormat("#0.00");
                                            numberFormat.setRoundingMode(RoundingMode.DOWN);
                                            double goalPercentage = (finalValueOfAllActivities /plannedActivity.getGoal())*100;
                                            String goalPercentageString = "(" + numberFormat.format(goalPercentage) + "%)";
                                            SpannableString ss = new SpannableString(goalPercentageString);

                                            if(goalPercentage >= 100) {
                                                ss.setSpan(new ForegroundColorSpan(Color.GREEN), 1, goalPercentageString.length() - 1, 0);
                                            }
                                            else {
                                                ss.setSpan(new ForegroundColorSpan(Color.RED), 1, goalPercentageString.length() - 1, 0);
                                            }

                                            holder.percentageDone.setText(ss);
                                        }
                                        else if(activityType.getStatisticsType().equals(StatisticsType.AVERAGE)) {
                                            DecimalFormat numberFormat = new DecimalFormat("#0.00");
                                            numberFormat.setRoundingMode(RoundingMode.DOWN);
                                            Double finalValueAverage = finalValueOfAllActivities/finalCountAverage;

                                            if(activityType.getValueType().equals(ValueTypes.MINUTES)){
                                                String stringSumMsg = convertToTime(finalValueAverage);
                                                String stringGoalTime = convertToTime(plannedActivity.getGoal());
                                                holder.activitiesDone.setText(stringSumMsg);
                                                holder.goal.setText(String.valueOf(stringGoalTime));
                                            }
                                            else {
                                                String activityDoneSum = numberFormat.format(finalValueAverage);
                                                String stringSumMsg = activityDoneSum;
                                                holder.activitiesDone.setText(activityDoneSum);
                                                holder.goal.setText(String.valueOf(plannedActivity.getGoal()));
                                            }


                                            double goalPercentage = (finalValueAverage /plannedActivity.getGoal())*100;
                                            String goalPercentageString = "(" + numberFormat.format(goalPercentage) + "%)";
                                            SpannableString ss = new SpannableString(goalPercentageString);
                                            if(goalPercentage >= 100) {
                                                ss.setSpan(new ForegroundColorSpan(Color.GREEN), 1, goalPercentageString.length() - 1, 0);
                                            }
                                            else {
                                                ss.setSpan(new ForegroundColorSpan(Color.RED), 1, goalPercentageString.length() - 1, 0);
                                            }
                                            holder.percentageDone.setText(ss);
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        holder.activitiesDone.setVisibility(View.GONE);
                        holder.percentageDone.setVisibility(View.GONE);
                        holder.activitiesDoneFix.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
        else {
            holder.addActivityDone.setVisibility(View.GONE);
            holder.activitiesDone.setVisibility(View.GONE);
            holder.percentageDone.setVisibility(View.GONE);
            holder.activitiesDoneFix.setVisibility(View.GONE);
        }


        holder.addActivityDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityDoneActivity.class);
                intent.putExtra(MODE, NEW);
                intent.putExtra(ActivityDoneActivity.PLANNED_ACTIVITY_ID, plannedActivity.getPushId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, goal, activitiesDone, percentageDone, activitiesDoneFix;
        Button deleteButton, editButton, addActivityDone;
        CardView cardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewAddedPlannedActivityDescription);
            goal = itemView.findViewById(R.id.textViewVariableGoal);
            deleteButton = itemView.findViewById((R.id.materialButtonDeletePlannedActivity));
            editButton = itemView.findViewById(R.id.materialButtonEditPlannedActivity);
            cardView = itemView.findViewById(R.id.cardViewPlannedActivities);
            addActivityDone = itemView.findViewById(R.id.materialButtonAddActivityDone);
            activitiesDone = itemView.findViewById(R.id.textViewVariableActivitiesDone);
            percentageDone = itemView.findViewById(R.id.textViewPercentageDone);
            activitiesDoneFix = itemView.findViewById(R.id.textViewFixActivitiesDone);

        }
    }

    public String convertToTime(double value) {
        int totalMinutes = (int) value;
        int hour = totalMinutes/60;
        int minutes = totalMinutes % 60;
        return hour + context.getString(R.string.h_hours) + minutes + context.getString(R.string.m_minutes);
    }

    public Double activitiesDone (String plannedActivityPushId) {
        return 0.0;
    }

}
