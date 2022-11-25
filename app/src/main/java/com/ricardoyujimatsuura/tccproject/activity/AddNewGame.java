package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.adapter.SpinnerAdapterEditGame;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.Game;

import java.util.ArrayList;
import java.util.Objects;

public class AddNewGame extends AppCompatActivity {

    //extras
    public static final String EDIT_GAME = "EDIT_GAME";
    public static final String MODE = "MODE";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    private Game editGameExtra;
    private int extraValueMode;


    //on create

    private FirebaseAuth auth;
    private Game game, gameSelected;
    private EditText editTextNewGameName, editTextNewGameDescription;
    private Spinner spinnerNewGameCategory;
    private String userId;

    //spinner edit game
    private ArrayList<Game> arrayListUserGame;


    //custom adapter para salvar jogo e mostrar o nome
    private SpinnerAdapterEditGame adapterEditGame;

    //category adapter
    private ArrayAdapter adapterCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_game);
        verifySignedInUser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userId = auth.getCurrentUser().getUid();

        editTextNewGameName = findViewById(R.id.editTextNewGameName);
        spinnerNewGameCategory = findViewById(R.id.spinnerNewGameCategory);
        editTextNewGameDescription = findViewById(R.id.editTextNewGameDescription);

        extraValueMode = getIntent().getExtras().getInt(MODE);

        if(auth != null) {
            populateCategorySpinner();
        }
        if(extraValueMode == EDIT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_game);
            editGameExtra = (Game) getIntent().getSerializableExtra(EDIT_GAME);
            editTextNewGameName.setText(editGameExtra.getName());
            editTextNewGameDescription.setText(editGameExtra.getDescription());
            int spinnerPosition = adapterCategory.getPosition(editGameExtra.getCategory());
            spinnerNewGameCategory.setSelection(spinnerPosition);


        }
        else if(extraValueMode == NEW) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_new_game);
        }

    }

    //verifica se o usuário está logado
    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //auth.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    //popula o spinner de categoria de jogos

    private void populateCategorySpinner() {
        String[] category = getResources().getStringArray(R.array.game_category); //cria um array com as categorias cadastradas no strings.xml
        adapterCategory = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category); // configuração do adapter do spiunner
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNewGameCategory.setAdapter(adapterCategory);


    }

    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_add_new_game, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menuButtonSaveGame) {
            if(extraValueMode == NEW) {
                saveNewGame();
                finish();
                return true;
            }
            if(extraValueMode == EDIT) {
                updateGame();
                finish();
                return true;
            }

        }  else if (itemId == android.R.id.home) {
            onBackPressed();
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);

    }

    public void saveNewGame() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        String name = editTextNewGameName.getText().toString();
        String description = editTextNewGameDescription.getText().toString();
        String category = spinnerNewGameCategory.getSelectedItem().toString();


        if(!name.isEmpty()) {
            if(!description.isEmpty()) {

                game = new Game();
                game.setName(name);
                game.setDescription(description);
                game.setCategory(category);
                game.setUserId(userId);
                game.saveToFirebase();

                finish();
                Toast.makeText(getApplicationContext(), getString(R.string.game_added), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(AddNewGame.this, getString(R.string.please_write_description), Toast.LENGTH_SHORT).show();
            }
        }
        else {

            Toast.makeText(AddNewGame.this, getString(R.string.please_write_game_name), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateGame() {
        String name = editTextNewGameName.getText().toString();
        String description = editTextNewGameDescription.getText().toString();
        String category = spinnerNewGameCategory.getSelectedItem().toString();


        if(!name.isEmpty()) {
            if(!description.isEmpty()) {
                Game gameEdit = new Game();
                gameEdit.setPushId(editGameExtra.getPushId());
                gameEdit.setName(name);
                gameEdit.setCategory(category);
                gameEdit.setDescription(description);
                gameEdit.setUserId(userId);
                gameEdit.updateGameToFirebase();
                finish();
                Toast.makeText(getApplicationContext(), getString(R.string.game_updated), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(AddNewGame.this, getString(R.string.please_write_description), Toast.LENGTH_LONG).show();
            }
        }
        else {

            Toast.makeText(AddNewGame.this, getString(R.string.please_write_game_name), Toast.LENGTH_LONG).show();
        }
    }


}