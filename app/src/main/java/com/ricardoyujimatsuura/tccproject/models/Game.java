package com.ricardoyujimatsuura.tccproject.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;

import java.io.Serializable;

public class Game implements Serializable {
    private String name;
    private String description;
    private String category;
    private String userId;
    private String pushId;

    public Game() {

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public void saveToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("game").child(this.userId).push().setValue(this);
    }

    public void updateGameToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("game").child(this.userId).child(this.pushId).setValue(this);
    }

    public void deleteFromFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("game").child(this.userId).child(this.pushId).removeValue();
    }


}
