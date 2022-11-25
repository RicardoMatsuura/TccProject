package com.ricardoyujimatsuura.tccproject.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;

import java.io.Serializable;

public class PlannedActivity implements Serializable {
    private String pushId;
    private String userId;
    private String trainingPlanId;
    private String activityTypeId;
    private String activityTypeName;
    private String description;
    private double goal;

    public PlannedActivity() {
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

    public String getTrainingPlanId() {
        return trainingPlanId;
    }

    public void setTrainingPlanId(String trainingPlanId) {
        this.trainingPlanId = trainingPlanId;
    }

    public String getActivityTypeId() {
        return activityTypeId;
    }

    public void setActivityTypeId(String activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    @Exclude
    public String getActivityTypeName() {
        return activityTypeName;
    }

    public void setActivityTypeName(String activityTypeName) {
        this.activityTypeName = activityTypeName;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void saveToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("plannedActivity").child(this.userId).push().setValue(this);
    }

    public void updateValueToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("plannedActivity").child(this.userId).child(this.pushId).setValue(this);
    }

    public String pushIdGenerated() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        return firebase.child("plannedActivity").child(this.userId).push().getKey();
    }
    public void saveToFirebaseWithPusId(String pushId) {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("plannedActivity").child(this.userId).child(pushId).setValue(this);
    }

    public void deleteFromFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("plannedActivity").child(this.userId).child(this.pushId).removeValue();
    }
}
