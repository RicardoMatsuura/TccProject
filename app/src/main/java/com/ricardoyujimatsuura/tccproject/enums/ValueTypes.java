package com.ricardoyujimatsuura.tccproject.enums;

import androidx.annotation.StringRes;

import com.ricardoyujimatsuura.tccproject.R;

public enum ValueTypes {
    NUMBERS(R.string.number_label), MINUTES(R.string.minutes_label);
    private final int strId;
    ValueTypes(int strId) {
        this.strId = strId;
    }
    @StringRes
    public int getStrId(){
        return strId;
    }
}
