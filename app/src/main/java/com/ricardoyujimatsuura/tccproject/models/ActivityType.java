package com.ricardoyujimatsuura.tccproject.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.enums.StatisticsType;
import com.ricardoyujimatsuura.tccproject.enums.ValueTypes;

import java.io.Serializable;

public class ActivityType implements Serializable {

    private String name;
    private String description;
    private ValueTypes valueType;
    private StatisticsType statisticsType;
    private String pushId;
    private String userId;

    public ActivityType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ValueTypes getValueType() {
        return valueType;
    }

    public void setValueType(ValueTypes valueType) {
        this.valueType = valueType;
    }

    public StatisticsType getStatisticsType() {
        return statisticsType;
    }

    public void setStatisticsType(StatisticsType statisticsType) {
        this.statisticsType = statisticsType;
    }
    @Exclude
    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public void saveToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityType").child(this.userId).push().setValue(this);
    }

    public void updateToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityType").child(this.userId).child(this.pushId).setValue(this);
    }

    public void deleteFromFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityType").child(this.userId).child(this.pushId).removeValue();
    }

}
