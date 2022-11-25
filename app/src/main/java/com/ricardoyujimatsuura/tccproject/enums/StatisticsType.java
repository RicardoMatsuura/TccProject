package com.ricardoyujimatsuura.tccproject.enums;

import androidx.annotation.StringRes;

import com.ricardoyujimatsuura.tccproject.R;

public enum StatisticsType {
    SUM(R.string.sum_label), AVERAGE(R.string.average_label);
    private final int strId;
            StatisticsType(int strId) {
                this.strId = strId;
            }
            @StringRes
            public int getStrId(){
                return strId;
            }
}
