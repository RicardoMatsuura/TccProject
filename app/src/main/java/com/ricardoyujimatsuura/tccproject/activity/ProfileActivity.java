package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.User;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private TextView profileName, profileNickname, profileEmail;

    //private User user;

    private String userId;

    private MenuItem editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        verifySignedInUser();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.profile);
        profileName = findViewById(R.id.textViewProfileName);
        profileNickname = findViewById(R.id.textViewProfileNickname);
        profileEmail = findViewById(R.id.textViewProfileEmail);

        //saveButton.setVisibility(View.GONE);
        if(auth != null) {
            getUserData();
        }



    }

    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //auth.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void getUserData() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        userId = auth.getCurrentUser().getUid();
        //user = new User();

        firebase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                    //user.setName(snapshot.child("name").getValue(String.class));
                    //user.setEmail(snapshot.child("nickname").getValue(String.class));
                profileEmail.setText(auth.getCurrentUser().getEmail());
                profileName.setText(user.getName());
                profileNickname.setText(user.getNickname());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menuButtonEditProfile) {
            goToEditProfileActivity();
            return true;

        }
        else if(itemId == R.id.menuItemChangePassword) {
            goToChangePasswordActivity();
            return true;
        }
        else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        editButton = menu.findItem(R.id.menuButtonEditProfile);
        return true;
    }

    public void goToEditProfileActivity() {
        startActivity(new Intent(this, EditProfile.class));
    }

    public void saveChangesProfile() {
        editButton.setVisible(true);;
        profileName.setVisibility(View.VISIBLE);
        profileNickname.setVisibility(View.VISIBLE);
        profileEmail.setVisibility(View.VISIBLE);
    }

    public void goToChangePasswordActivity() {
        startActivity(new Intent(this, ChangePasswordActivity.class));
    }
}