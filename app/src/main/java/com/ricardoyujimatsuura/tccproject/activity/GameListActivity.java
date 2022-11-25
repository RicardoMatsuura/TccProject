package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewGameList;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.Game;

import java.util.ArrayList;
import java.util.Objects;

public class GameListActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView recyclerViewGameList;
    private ArrayList<Game> arrayListGames;
    private String userId;
    private AdapterRecyclerViewGameList adapterRecyclerViewGameList;

    //extras e intent

    public static final String MODE = "MODE";
    public static final int NEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        verifySignedInUser();
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.games);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerViewGameList = findViewById(R.id.recyclerViewGameList);
        recyclerViewGameList.setHasFixedSize(true);
        recyclerViewGameList.setLayoutManager(new LinearLayoutManager(this));

        if(auth != null) {
            userId = auth.getCurrentUser().getUid();
            populateGameListRecyclerView();
        }



    }

    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void populateGameListRecyclerView() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        arrayListGames = new ArrayList<>();

        firebase.child("game").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListGames.clear();
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Game game = childSnapshot.getValue(Game.class);
                    game.setPushId(childSnapshot.getKey());
                    game.setUserId(userId);
                    arrayListGames.add(game);
                }
                adapterRecyclerViewGameList = new AdapterRecyclerViewGameList(GameListActivity.this, arrayListGames);
                recyclerViewGameList.setAdapter(adapterRecyclerViewGameList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_game_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if(itemId == R.id.menuItemAddNewGameListActivity) {
            Intent intent = new Intent(this, AddNewGame.class);
            intent.putExtra(MODE, NEW);
            startActivity(intent);
            return true;
        } else if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}