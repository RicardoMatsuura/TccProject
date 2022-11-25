package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ricardoyujimatsuura.tccproject.models.ActivityType;

import java.util.ArrayList;

public class SpinnerAdapterActivityType extends ArrayAdapter {

    private Context context;
    private ArrayList<ActivityType> values;


    public SpinnerAdapterActivityType(@NonNull Context context, int resource, @NonNull ArrayList<ActivityType> values) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getName());

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getName());

        return label;
    }

}
