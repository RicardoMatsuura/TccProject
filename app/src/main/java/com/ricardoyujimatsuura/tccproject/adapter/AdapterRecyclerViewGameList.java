package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
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
import com.ricardoyujimatsuura.tccproject.activity.AddNewGame;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.Game;
import com.ricardoyujimatsuura.tccproject.models.TrainingPlan;

import java.util.ArrayList;

public class AdapterRecyclerViewGameList extends RecyclerView.Adapter<AdapterRecyclerViewGameList.MyViewHolder>{

    ArrayList<Game> list;
    Context context;
    FirebaseAuth auth;
    private String userId;

    //extras

    public static final String MODE = "MODE";
    public static final String EDIT_GAME = "EDIT_GAME";
    public static final int NEW = 1;
    public static final int EDIT = 2;

    public AdapterRecyclerViewGameList(Context context, ArrayList<Game> list) {
        this.context = context;
        this.list = list;

    }

    @NonNull
    @Override
    public AdapterRecyclerViewGameList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_game_list, parent, false);
        auth = FirebaseConfig.getFirebaseAuth();
        userId = auth.getCurrentUser().getUid();
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecyclerViewGameList.MyViewHolder holder, int position) {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        Game game = list.get(position);
        holder.name.setText(game.getName());
        holder.description.setText(game.getDescription());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(context);
                builder.setMessage(Html.fromHtml(context.getString(R.string.delete_game_bold)
                        + game.getName()
                        + context.getString(R.string.associated_training_plan)));
                builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        game.deleteFromFirebase();
                        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
                        firebase.child("trainingPlan").child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                                    TrainingPlan trainingPlan;
                                    trainingPlan = childSnapshot.getValue(TrainingPlan.class);
                                    trainingPlan.setUserId(userId);
                                    trainingPlan.setPushId(childSnapshot.getKey());
                                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                                    if(game.getPushId().equals(trainingPlan.getGameId())){
                                        trainingPlan.deleteFromFirebase();
                                        Toast.makeText(context, context.getString(R.string.training_plan_deleted), Toast.LENGTH_SHORT).show();
                                    }

                                }
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
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddNewGame.class);
                intent.putExtra(MODE, EDIT);
                intent.putExtra(EDIT_GAME, game);
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

            name = itemView.findViewById(R.id.textViewGameNameRecycler);
            description = itemView.findViewById(R.id.textViewGameDescriptionRecycler);
            edit = itemView.findViewById(R.id.materialButtonEditGame);
            delete = itemView.findViewById(R.id.materialButtonDeleteGame);

        }
    }
}
