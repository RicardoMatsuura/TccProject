package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;

import java.util.ArrayList;

public class AdapterRecyclerViewGameActivity extends RecyclerView.Adapter<AdapterRecyclerViewGameActivity.MyViewHolder>{

    Context context;
    ArrayList<ActivityType> list;


    public AdapterRecyclerViewGameActivity(Context context, ArrayList<ActivityType> list, ArrayList<ActivityType> checkedList) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_game_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ActivityType activityType = list.get(position);
        holder.name.setText(activityType.getName());
        holder.description.setText(activityType.getDescription());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewGameNameRecycler);
            description = itemView.findViewById(R.id.textViewGameDescriptionRecycler);

        }
    }

}
