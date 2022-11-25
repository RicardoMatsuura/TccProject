package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewTrainingPlan;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.TrainingPlan;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth auth;

    //recycler view dos planos de treino ativos
    private RecyclerView recyclerViewActiveTrainingPlan;
    private AdapterRecyclerViewTrainingPlan adapterRecyclerViewTrainingPlan;
    private ArrayList<TrainingPlan> arrayListTrainingPlan;
    private String userId;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    //extras e intent

    public static final String MODE = "MODE";
    public static final String EDIT_ACTIVITY_TYPE = "EDIT_ACTIVITY_TYPE";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    public static final String SHARED_PASSWORD = "SHARED_PASSWORD";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifySignedInUser();
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.active_training_plans);

        //recycler view
        recyclerViewActiveTrainingPlan = findViewById(R.id.recyclerViewActiveTrainingPlan);
        recyclerViewActiveTrainingPlan.setHasFixedSize(true);
        recyclerViewActiveTrainingPlan.setLayoutManager(new LinearLayoutManager(this));

        //drawer layout

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation view

        navigationView = findViewById(R.id.navigationViewMain);
        navigationView.setNavigationItemSelectedListener(this);



        if(auth.getCurrentUser() != null) {
            populateActiveTrainingPlansRecycler();
        }

        FloatingActionButton fab = findViewById(R.id.fabItemAddTrainingPlan);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 goToAddTrainingPlanActivity();
            }
        });
    }

    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void goToAddTrainingPlanActivity() {

        Intent intent = new Intent(this, AddTrainingPlan.class);
        intent.putExtra(MODE, NEW);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifySignedInUser();
    }

    public void goToProfileActivity() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public void populateActiveTrainingPlansRecycler() {

        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        userId = auth.getCurrentUser().getUid();
        arrayListTrainingPlan = new ArrayList<>();

        firebase.child("trainingPlan").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListTrainingPlan.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.child("active").getValue().equals(true)) {
                        TrainingPlan trainingPlan = dataSnapshot.getValue(TrainingPlan.class);
                        trainingPlan.setPushId(dataSnapshot.getKey());
                        trainingPlan.setUserId(userId);
                        arrayListTrainingPlan.add(trainingPlan);
                    }

                }

                for(TrainingPlan trainingPlan : arrayListTrainingPlan) {
                    firebase.child("game").child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if(dataSnapshot.getKey().equals(trainingPlan.getGameId())) {
                                    trainingPlan.setGame(dataSnapshot.child("name").getValue(String.class));
                                    //Log.i("NOME DO JOGO: ", trainingPlan.getGame());
                                }
                            }
                            adapterRecyclerViewTrainingPlan = new AdapterRecyclerViewTrainingPlan(MainActivity.this, arrayListTrainingPlan);
                            recyclerViewActiveTrainingPlan.setAdapter(adapterRecyclerViewTrainingPlan);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    firebase.child("game").child("admin").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if(dataSnapshot.getKey().equals(trainingPlan.getGameId())) {
                                    trainingPlan.setGame(dataSnapshot.child("name").getValue(String.class));

                                }
                            }
                            adapterRecyclerViewTrainingPlan = new AdapterRecyclerViewTrainingPlan(MainActivity.this, arrayListTrainingPlan);
                            recyclerViewActiveTrainingPlan.setAdapter(adapterRecyclerViewTrainingPlan);
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuButtonProfileMainActivity) {
            goToProfileActivity();
            return true;
        } else if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuItemGameListMainActivity) {
            startActivity(new Intent(MainActivity.this, GameListActivity.class));
            return true;
        } else if(item.getItemId() == R.id.menuItemActivityTypeListMainActivity) {
            startActivity(new Intent(MainActivity.this, ActivityTypeList.class));
            return true;
        }
        else if(item.getItemId() == R.id.menuItemLogout) {
            auth.signOut();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PASSWORD, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            goToLoginActivity();

        }
        return true;
    }



}