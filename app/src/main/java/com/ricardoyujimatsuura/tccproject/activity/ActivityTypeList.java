package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewActivityTypeList;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;

import java.util.ArrayList;
import java.util.Objects;

public class ActivityTypeList extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView recyclerViewActivityType;
    private ArrayList<ActivityType> arrayListActivityType;
    private String userId;
    private AdapterRecyclerViewActivityTypeList adapterRecyclerViewActivityTypeList;

    public static final String MODE = "MODE";
    public static final int NEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_list);
        verifySignedInUser();
        recyclerViewActivityType = findViewById(R.id.recyclerViewActivityTypeList);
        recyclerViewActivityType.setHasFixedSize(true);
        recyclerViewActivityType.setLayoutManager(new LinearLayoutManager(this));



        if(auth != null) {
            userId = auth.getCurrentUser().getUid();
            populateActivityTypeRecyclerView();
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.activity_types);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    public void populateActivityTypeRecyclerView() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        arrayListActivityType = new ArrayList<>();

        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListActivityType.clear();
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                    activityType.setPushId(childSnapshot.getKey());
                    activityType.setUserId(userId);
                    arrayListActivityType.add(activityType);
                }
                adapterRecyclerViewActivityTypeList = new AdapterRecyclerViewActivityTypeList(ActivityTypeList.this, arrayListActivityType);
                recyclerViewActivityType.setAdapter(adapterRecyclerViewActivityTypeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_activity_type_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int menuId = item.getItemId();

        if(menuId == R.id.menuItemAddNewActivityTypeListActivity) {
            Intent intent = new Intent(this, AddNewActivityType.class);
            intent.putExtra(MODE, NEW);
            startActivity(intent);
            return true;
        } else if(menuId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}