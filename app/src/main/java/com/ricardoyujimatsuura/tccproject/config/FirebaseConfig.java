package com.ricardoyujimatsuura.tccproject.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfig {
    private static FirebaseAuth auth;
    private static DatabaseReference firebaseReference;

    public static FirebaseAuth getFirebaseAuth() {
        if(auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;

    }

    public static DatabaseReference getFirebaseDatabase() {
        if(firebaseReference == null) {
            firebaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return firebaseReference;
    }
}
