package com.ricardoyujimatsuura.tccproject.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;

import java.io.Serializable;

public class ActivityDone implements Serializable {

    private String userId;
    private String pushId;
    private String plannedActivityId;
    private Double value;
    private String note;
    private Long timestamp;
    private String activityTypeAuxId;

    public ActivityDone() {
    }
    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    @Exclude
    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getPlannedActivityId() {
        return plannedActivityId;
    }

    public void setPlannedActivityId(String plannedActivityId) {
        this.plannedActivityId = plannedActivityId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public String getActivityTypeAuxId() {
        return activityTypeAuxId;
    }

    public void setActivityTypeAuxId(String activityTypeAuxId) {
        this.activityTypeAuxId = activityTypeAuxId;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void saveToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityDone").child(this.userId).push().setValue(this);
    }

    public void updateToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityDone").child(this.userId).child(this.pushId).setValue(this);
    }

    public void deleteFromFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityDone").child(this.userId).child(this.pushId).removeValue();
    }
}
