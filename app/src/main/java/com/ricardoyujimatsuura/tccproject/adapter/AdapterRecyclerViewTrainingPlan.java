package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import com.ricardoyujimatsuura.tccproject.activity.AddTrainingPlan;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.ActivityDone;
import com.ricardoyujimatsuura.tccproject.models.PlannedActivity;
import com.ricardoyujimatsuura.tccproject.models.TrainingPlan;

import java.util.ArrayList;

public class AdapterRecyclerViewTrainingPlan extends RecyclerView.Adapter<AdapterRecyclerViewTrainingPlan.MyViewHolder>{

    Context context;

    ArrayList<TrainingPlan> list;

    ArrayList<String> arrayListPlannedActivityIdDelete;

    public static final String MODE = "MODE";
    public static final String EDIT_TRAINING_PLAN = "EDIT_TRAINING_PLAN";
    public static final int NEW = 1;
    public static final int EDIT = 2;

    public AdapterRecyclerViewTrainingPlan(Context context, ArrayList<TrainingPlan> list) {
        this.context = context;
        this.list = list;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_active_trainingplan, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
        String userId = auth.getCurrentUser().getUid();
        TrainingPlan trainingPlan = list.get(position);
        holder.name.setText(trainingPlan.getName());
        holder.description.setText(trainingPlan.getDescription());
        holder.game.setText(trainingPlan.getGame());


        //menu para deletar e editar itens do recycler view
        holder.menu.setOnClickListener(v-> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menu);
            popupMenu.inflate(R.menu.menu_recycler_view_training_plan);
            popupMenu.setOnMenuItemClickListener(item-> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_trainingPlan_edit) {

                    Intent intent = new Intent(context, AddTrainingPlan.class);
                    intent.putExtra(MODE, EDIT);
                    intent.putExtra(EDIT_TRAINING_PLAN, trainingPlan);
                    context.startActivity(intent);
                } else if (itemId == R.id.menu_trainingPlan_delete) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Html.fromHtml(context.getString(R.string.delete_training_plan_bold)
                            + trainingPlan.getName()
                            + context.getString(R.string.planned_activities_and_activites_done_related)));
                    builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            firebase.child("plannedActivity").child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    arrayListPlannedActivityIdDelete = new ArrayList<>();
                                    for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        PlannedActivity plannedActivityAux;
                                        plannedActivityAux = childSnapshot.getValue(PlannedActivity.class);
                                        plannedActivityAux.setUserId(userId);
                                        plannedActivityAux.setPushId(childSnapshot.getKey());
                                        if(plannedActivityAux.getTrainingPlanId() != null
                                                && trainingPlan.getPushId().equals(plannedActivityAux.getTrainingPlanId())) {

                                                plannedActivityAux.deleteFromFirebase();

                                                firebase.child("activityDone").child(userId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                   for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                       ActivityDone activityDone = childSnapshot.getValue(ActivityDone.class);
                                                       activityDone.setUserId(userId);
                                                       activityDone.setPushId(childSnapshot.getKey());
                                                       if(plannedActivityAux.getPushId().equals(activityDone.getPlannedActivityId())) {
                                                           activityDone.deleteFromFirebase();
                                                       }
                                                   }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                    }
                                    trainingPlan.deleteFromFirebase();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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
                return false;
            });
            popupMenu.show();
        });

        //clicar em cada card view e ir para o plano de treino
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddTrainingPlan.class);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_TRAINING_PLAN, trainingPlan);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, description, game, menu;
        CardView cardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewTrainingPlanNameRecycler);
            description = itemView.findViewById(R.id.textViewTrainingPlanDescriptionRecycler);
            game = itemView.findViewById(R.id.textViewTrainingPlanGameRecycler);
            menu = itemView.findViewById(R.id.textViewMenuOptionsRecycler);
            cardView = itemView.findViewById(R.id.cardViewActiveTrainingPlan);

        }
    }
}
