package com.ricardoyujimatsuura.tccproject.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;

import java.io.Serializable;
import java.util.ArrayList;

public class TrainingPlan implements Serializable {

    private String userId;
    private String name;
    private String description;
    private String game;
    private String createdAt;
    private boolean isActive;
    private ArrayList<String> activitiesAdded;
    private String pushId;
    private String gameId;

    public TrainingPlan() {
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String idFirebase) {
        this.userId = idFirebase;
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

    @Exclude
    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ArrayList<String> getActivitiesAdded() {
        return activitiesAdded;
    }

    public void setActivitiesAdded(ArrayList<String> activitiesAdded) {
        this.activitiesAdded = activitiesAdded;
    }

    @Exclude
    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    /*public void setTimestamp(Map<String, String> timeStamp) {this.timestamp= timestamp;}

    public Map<String, String> getTimestamp() {return timestamp;}*/

    public void saveToFirebase(String pushId) {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("trainingPlan").child(this.userId).child(pushId).setValue(this);
    }

    public String pushIdGenerated() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        return firebase.child("trainingPlan").child(this.userId).push().getKey();
    }

    public void updateValueToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("trainingPlan").child(this.userId).child(this.pushId).setValue(this);

    }

    public void deleteFromFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("trainingPlan").child(this.userId).child(this.pushId).removeValue();
    }

    public void deleteIfGameIsDeleted() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("trainingPlan").child(this.userId).child(getPushId()).removeValue();
    }

}
