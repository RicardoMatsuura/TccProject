package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.activity.AddNewActivityType;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;
import com.ricardoyujimatsuura.tccproject.models.PlannedActivity;

import java.util.ArrayList;

public class AdapterRecyclerViewActivityTypeList extends RecyclerView.Adapter<AdapterRecyclerViewActivityTypeList.MyViewHolder>{

    ArrayList<ActivityType> list;
    Context context;
    FirebaseAuth auth;
    private String userId;

    public static final String MODE = "MODE";
    public static final String EDIT_ACTIVITY_TYPE = "EDIT_ACTIVITY_TYPE";
    public static final int NEW = 1;
    public static final int EDIT = 2;

    public AdapterRecyclerViewActivityTypeList(Context context, ArrayList<ActivityType> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterRecyclerViewActivityTypeList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_activity_type_list, parent, false);
        auth = FirebaseConfig.getFirebaseAuth();
        userId = auth.getCurrentUser().getUid();
        return new AdapterRecyclerViewActivityTypeList.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecyclerViewActivityTypeList.MyViewHolder holder, int position) {
        ActivityType activityType = list.get(position);
        holder.name.setText(activityType.getName());
        holder.description.setText(activityType.getDescription());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Html.fromHtml(context.getString(R.string.delete_the_activity_type_bold)
                        + activityType.getName()
                        + context.getString(R.string.planned_activities_associeated)));
                builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        activityType.deleteFromFirebase();
                        //deleta todas as atividades planejadas que estão associadas a esse tipo de atividade
                        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
                        firebase.child("plannedActivity").child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                                    PlannedActivity plannedActivity;
                                    plannedActivity = childSnapshot.getValue(PlannedActivity.class);
                                    plannedActivity.setUserId(userId);
                                    plannedActivity.setPushId(childSnapshot.getKey());
                                    if(activityType.getPushId().equals(plannedActivity.getActivityTypeId())) {
                                        plannedActivity.deleteFromFirebase();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //Toast.makeText(AddNewGame.this, "The game has been deleted!", Toast.LENGTH_SHORT).show();
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
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddNewActivityType.class);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_ACTIVITY_TYPE, activityType);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, description;
        Button delete, edit;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewActivityTypeNameRecycler);
            description = itemView.findViewById(R.id.textViewActivityTypeDescriptionRecycler);
            edit = itemView.findViewById(R.id.materialButtonEditActivityType);
            delete = itemView.findViewById(R.id.materialButtonDeleteActivityType);

        }
    }
}
