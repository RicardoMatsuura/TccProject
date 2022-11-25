package com.ricardoyujimatsuura.tccproject.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ricardoyujimatsuura.tccproject.enums.StatisticsType;

public class SpinnerAdapterStatisticType extends ArrayAdapter<StatisticsType> {

    public SpinnerAdapterStatisticType(@NonNull Context context, int resource) {
        super(context, resource, StatisticsType.values());

    }

    @Override
    public int getCount() {
        return super.getCount();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(getItem(position).getStrId());

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(getItem(position).getStrId());

        return label;
    }
}
