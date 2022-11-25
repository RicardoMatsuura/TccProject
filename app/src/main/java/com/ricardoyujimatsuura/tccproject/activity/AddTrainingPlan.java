package com.ricardoyujimatsuura.tccproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewAddedPlannedActivity;
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewGameActivity;
import com.ricardoyujimatsuura.tccproject.adapter.SpinnerAdapterEditGame;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.helper.DateUtil;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;
import com.ricardoyujimatsuura.tccproject.models.Game;
import com.ricardoyujimatsuura.tccproject.models.PlannedActivity;
import com.ricardoyujimatsuura.tccproject.models.TrainingPlan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class AddTrainingPlan extends AppCompatActivity {

    //put extras
    public static final String MODE = "MODE";
    public static final String EDIT_TRAINING_PLAN = "EDIT_TRAINING_PLAN";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    public static final int VIEW = 3;
    public static final String TRAINING_PLAN_ID = "TRAINING_PLAN_ID";
    private int extraValueMode;
    private TrainingPlan trainingPlan_edit;

    //autenticação do firebase
    private FirebaseAuth auth;

    //tela principal
    private TextView textViewAddNewGame;
    private EditText editTextTrainingPlanName, editTextTrainingPlanDescription;
    private Spinner spinnerGame;
    private ArrayList<String> spinnerGameArrayList;
    private String userId, createdAt;
    private TrainingPlan trainingPlan;
    private ArrayAdapter<String> adapter;
    private String compareValue;
    private String createdAtAux, endDateAux, pushIdAux;
    private Button buttonListGames, buttonAddPlannedActivities;


    //melhorar depois

    //dialog para atividades
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private RecyclerView recyclerViewGameActivity;
    private AdapterRecyclerViewGameActivity adapterGameActivity;
    ArrayList<ActivityType> arrayListActivityType;
    ArrayList<ActivityType> arrayListChecked;
    private TextView textViewPlannedActivities;

    //dialog para adicionar novas atividades
    private AlertDialog.Builder dialogBuilderNewActivities;
    private AlertDialog dialogNewActivities;


    //campos para adicionar uma nova atividade existente
    private EditText editTextNewGameActivityName, editTextNewGameActivityDescription;
    private Spinner spinnerValueType, spinnerStatisticType;
    private ActivityType activityType;

    //popular o recycler view das atividades adicionadas ao plano de treino
    private AdapterRecyclerViewAddedPlannedActivity adapterAddedGameActivity;
    private RecyclerView recyclerViewAddedActivities;

    //Array para pegar o valor do pushId e inserir no plano de treino

    private ArrayList<String> arrayPushId;

    //teste intent


    //Game Adapter
    private SpinnerAdapterEditGame adapterGame;
    private ArrayList<Game> arrayListGame;

    //adicionar as atividades planejadas no arraylist
    ArrayList<PlannedActivity> plannedActivities;

    //shared pref
    private ArrayList<PlannedActivity> arrayListPlannedActivitiesEdit;
    public static final String ADDED_PLANNED_ACTIVITY = "PLANNED_ACTIVITY";
    private ActivityResultLauncher<Intent> mGetContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training_plan);
        verifySignedInUser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editTextTrainingPlanName = findViewById(R.id.editTextTrainingPlanName);
        editTextTrainingPlanDescription = findViewById(R.id.editTextTrainingPlanDescription);
        spinnerGame = findViewById(R.id.spinnerGame);
        textViewPlannedActivities = findViewById(R.id.textViewPlannedActivitiesAdded);
        buttonListGames = findViewById(R.id.materialButtonListGames);
        buttonAddPlannedActivities = findViewById(R.id.materialButtonAddPlannedActivities);
        userId = auth.getCurrentUser().getUid();
        //melhorar depois
        trainingPlan = new TrainingPlan();

        extraValueMode = getIntent().getExtras().getInt(MODE);

        //recycler view

        recyclerViewAddedActivities = findViewById(R.id.recyclerViewAddedActivities);
        recyclerViewAddedActivities.setHasFixedSize(true);
        recyclerViewAddedActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAddedActivities.setVisibility(View.VISIBLE);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_training_plan);

        //intent com result novo
        mGetContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            //Gson gson = new Gson();
                            result.getData();
                            Intent intent =  result.getData();
                            Bundle args = intent.getBundleExtra("BUNDLE");
                            arrayListPlannedActivitiesEdit = (ArrayList<PlannedActivity>) args.getSerializable("ARRAYLIST");
                            recyclerViewAddedActivities.setVisibility(View.VISIBLE);
                            populateRecyclerViewAddedGameActivity();
                        }
                    }
                });

        buttonListGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToListGameActivity();
            }
        });

        buttonAddPlannedActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlannedActivitiesActivity();
            }
        });

        if(extraValueMode == EDIT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_training_plan);
            trainingPlan_edit = (TrainingPlan) getIntent().getSerializableExtra(EDIT_TRAINING_PLAN);

            populateEditRecyclerViewPlannedActivities();
            compareValue = trainingPlan_edit.getGameId();
            populateGameSpinnerEdit();
            spinnerGame.setEnabled(false);
            editTextTrainingPlanName.setText(trainingPlan_edit.getName());
            editTextTrainingPlanDescription.setText(trainingPlan_edit.getDescription());



        }
        else if(extraValueMode == NEW){
            populateGameSpinner();
            if(arrayListPlannedActivitiesEdit == null) {
                arrayListPlannedActivitiesEdit = new ArrayList<>();
            }

        }
        else if(extraValueMode == VIEW) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Training Plan");
            trainingPlan_edit = (TrainingPlan) getIntent().getSerializableExtra(EDIT_TRAINING_PLAN);
            populateEditRecyclerViewPlannedActivities();
            compareValue = trainingPlan_edit.getGameId();
            populateGameSpinnerEdit();
            editTextTrainingPlanName.setText(trainingPlan_edit.getName());
            editTextTrainingPlanDescription.setText(trainingPlan_edit.getDescription());
            editTextTrainingPlanDescription.setEnabled(false);
            editTextTrainingPlanName.setEnabled(false);
            spinnerGame.setEnabled(false);
        }


    }

    //ir para a lista de jogos cadastrados
    public void goToListGameActivity() {
        startActivity(new Intent(this, GameListActivity.class));
    }

    //verifica se o usuário está logado
    public void verifySignedInUser() {
        auth = FirebaseConfig.getFirebaseAuth();

        //auth.signOut();
        if ( auth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    //popula o spinner de jogos
    private void populateGameSpinner() {

        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        arrayListGame = new ArrayList<Game>();

        firebase.child("game").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListGame.clear();
                firebase.child("game").child("admin").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                            Game game;
                            game = childSnapshot.getValue(Game.class);
                            game.setPushId(childSnapshot.getKey());
                            arrayListGame.add(game);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Game game;
                    game = childSnapshot.getValue(Game.class);
                    game.setPushId(childSnapshot.getKey());
                    arrayListGame.add(game);
                }
                adapterGame = new SpinnerAdapterEditGame(AddTrainingPlan.this,
                        android.R.layout.simple_spinner_item, arrayListGame);
                adapterGame.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGame.setAdapter(adapterGame);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //popula o jogo quando se está editando um plano de treino
    private void populateGameSpinnerEdit() {

        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        arrayListGame = new ArrayList<Game>();

        firebase.child("game").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListGame.clear();
                firebase.child("game").child("admin").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                            //Log.i("NAME: ", "CHEGOU");
                            Game game;
                            game = childSnapshot.getValue(Game.class);
                            game.setPushId(childSnapshot.getKey());
                            arrayListGame.add(game);
                        }
                        adapterGame = new SpinnerAdapterEditGame(AddTrainingPlan.this,
                                android.R.layout.simple_spinner_item, arrayListGame);
                        adapterGame.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGame.setAdapter(adapterGame);
                        for(int i = 0; i < adapterGame.getCount(); i++) {
                            Game game = (Game) adapterGame.getItem(i);
                            if(game.getPushId().equals(compareValue)) {
                                spinnerGame.setSelection(i);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Game game;
                    game = childSnapshot.getValue(Game.class);
                    game.setPushId(childSnapshot.getKey());
                    arrayListGame.add(game);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //método para salvar no database o novo plano de treino
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveNewTrainingPlan() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();

        Game gameEdit = (Game) adapterGame.getItem(spinnerGame.getSelectedItemPosition());
        String name = editTextTrainingPlanName.getText().toString();
        String description = editTextTrainingPlanDescription.getText().toString();

        createdAt = DateUtil.currentDateTest();


        if(!name.isEmpty()) {
            if(!description.isEmpty()) {

                trainingPlan.setName(name);
                trainingPlan.setDescription(description);
                trainingPlan.setUserId(userId);
                trainingPlan.setGameId(gameEdit.getPushId());
                trainingPlan.setCreatedAt(createdAt);
                trainingPlan.setActive(true);

                firebase.child("trainingPlan").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.child("gameId").getValue().toString().equals(trainingPlan.getGameId()) && dataSnapshot.child("active").getValue().equals(true)) {
                                    count++;
                                    Toast.makeText(AddTrainingPlan.this, R.string.active_training_plan_for_game, Toast.LENGTH_SHORT).show();
                                    break;
                                }

                            }
                            if (count == 0) {
                                String trainingPlanId = trainingPlan.pushIdGenerated();
                                if(arrayListPlannedActivitiesEdit != null) {
                                    for(PlannedActivity plannedActivity : arrayListPlannedActivitiesEdit) {
                                        plannedActivity.setTrainingPlanId(trainingPlanId);
                                        plannedActivity.setUserId(userId);
                                        plannedActivity.saveToFirebase();
                                    }
                                }
                                trainingPlan.saveToFirebase(trainingPlanId);
                                finish();


                                Toast.makeText(getApplicationContext(), R.string.training_plan_added, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String trainingPlanId = trainingPlan.pushIdGenerated();
                            for(PlannedActivity plannedActivity : arrayListPlannedActivitiesEdit) {
                                plannedActivity.setUserId(userId);
                                plannedActivity.setTrainingPlanId(trainingPlanId);
                                plannedActivity.saveToFirebase();

                            }
                            trainingPlan.saveToFirebase(trainingPlanId);
                            Toast.makeText(getApplicationContext(), R.string.training_plan_added, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else {
                Toast.makeText(AddTrainingPlan.this, R.string.please_write_description, Toast.LENGTH_SHORT).show();
            }
        }
        else {

            Toast.makeText(AddTrainingPlan.this, R.string.please_write_name_training_plan, Toast.LENGTH_SHORT).show();
        }


    }

    //atualiza os dados de um plano de treino quando for editado
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateValuesToFirebase() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();

        Game gameEdit = (Game) adapterGame.getItem(spinnerGame.getSelectedItemPosition());
        String name = editTextTrainingPlanName.getText().toString();
        String description = editTextTrainingPlanDescription.getText().toString();
        createdAt = createdAtAux;

        if(!name.isEmpty()) {
            if(!description.isEmpty()) {

                trainingPlan.setName(name);
                trainingPlan.setDescription(description);
                trainingPlan.setUserId(userId);
                trainingPlan.setGameId(gameEdit.getPushId());
                trainingPlan.setCreatedAt(createdAt);
                trainingPlan.setPushId(pushIdAux);
                trainingPlan.setActive(true);
                trainingPlan.updateValueToFirebase();

                finish();

            }
            else {
                Toast.makeText(AddTrainingPlan.this, R.string.please_write_description, Toast.LENGTH_SHORT).show();
            }
        }
        else {

            Toast.makeText(AddTrainingPlan.this, R.string.please_write_name_training_plan, Toast.LENGTH_SHORT).show();
        }


    }

    //vai para a tela de login se o usuário não estiver autenticado
    public void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    //popula o recycler view de atividades feitas ao voltar da tela de atividade planejada
    public void populateRecyclerViewAddedGameActivity() {
        adapterAddedGameActivity = new AdapterRecyclerViewAddedPlannedActivity(arrayListPlannedActivitiesEdit, this, mGetContent);
        recyclerViewAddedActivities.setAdapter(adapterAddedGameActivity);
    }

    //popula o recycler view de atividades planejadas quando for em modo de edição do plano de treino
    public void populateEditRecyclerViewPlannedActivities() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("plannedActivity").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListPlannedActivitiesEdit = new ArrayList<>();
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PlannedActivity plannedActivity;
                    plannedActivity = childSnapshot.getValue(PlannedActivity.class);
                    plannedActivity.setUserId(userId);
                    plannedActivity.setPushId(childSnapshot.getKey());
                    if(trainingPlan_edit.getPushId().equals(plannedActivity.getTrainingPlanId())) {
                        arrayListPlannedActivitiesEdit.add(plannedActivity);
                    }
                }

                if(!arrayListPlannedActivitiesEdit.isEmpty()) {
                    firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                for(PlannedActivity plannedActivity : arrayListPlannedActivitiesEdit) {
                                    for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                                        if(plannedActivity.getActivityTypeId().equals(childSnapshot.getKey())) {
                                            plannedActivity.setActivityTypeName(activityType.getName());
                                        }
                                    }
                                }

                                adapterAddedGameActivity= new AdapterRecyclerViewAddedPlannedActivity(arrayListPlannedActivitiesEdit, AddTrainingPlan.this, mGetContent);
                                recyclerViewAddedActivities.setAdapter(adapterAddedGameActivity);
                                adapterAddedGameActivity.notifyDataSetChanged();

                            }

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
        getMenuInflater().inflate(R.menu.options_menu_add_training_plan, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menuButtonSaveTrainingPlan) {
            if (extraValueMode == NEW) {
                saveNewTrainingPlan();
            } else if (extraValueMode == EDIT) {
                createdAtAux = trainingPlan_edit.getCreatedAt();
                pushIdAux = trainingPlan_edit.getPushId();
                updateValuesToFirebase();

            }
            else if(extraValueMode == VIEW) {
                createdAtAux = trainingPlan_edit.getCreatedAt();
                pushIdAux = trainingPlan_edit.getPushId();
                updateValuesToFirebase();
            }
            return true;
        }
        else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    //método para o botão de adicionar novas atividades planejadas
    public void addPlannedActivitiesActivity() {
        Intent intent = new Intent(AddTrainingPlan.this, AddPlannedActivity.class);
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST", (Serializable) arrayListPlannedActivitiesEdit);
        intent.putExtra("BUNDLE", args);
        intent.putExtra(MODE, NEW);
        if(extraValueMode == EDIT) {
            intent.putExtra(TRAINING_PLAN_ID, trainingPlan_edit.getPushId());
        }
        mGetContent.launch(intent);
    }

}