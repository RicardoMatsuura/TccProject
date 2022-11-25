package com.ricardoyujimatsuura.tccproject.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewActivityDoneList;
import com.ricardoyujimatsuura.tccproject.adapter.AdapterRecyclerViewGameActivity;
import com.ricardoyujimatsuura.tccproject.adapter.SpinnerAdapterActivityType;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.enums.StatisticsType;
import com.ricardoyujimatsuura.tccproject.enums.ValueTypes;
import com.ricardoyujimatsuura.tccproject.models.ActivityDone;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;
import com.ricardoyujimatsuura.tccproject.models.PlannedActivity;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class AddPlannedActivity extends AppCompatActivity {


    public static final String ADDED_PLANNED_ACTIVITY = "PLANNED_ACTIVITY";

    private FirebaseAuth auth;
    private String userId;

    private RecyclerView recyclerViewGameActivity;
    private AdapterRecyclerViewGameActivity adapterGameActivity;
    ArrayList<ActivityType> spinnerActivityTypeArrayList;

    //private ArrayAdapter<String> adapter;
    private Spinner spinnerActivityType;

    //custom spinner adapter para salvar a activity type e mostrar o nome
    private SpinnerAdapterActivityType spinnerAdapter;
    private EditText editTextDescription;
    //<GameActivity> arrayListChecked;


    //Edit Text da meta
    private EditText editTextGoal;//Number, editTextGoalTime;

    //Array List para guardar na shared preferences todas as atividades planejadas do usuário
    private ArrayList<PlannedActivity> arrayListPlannedActivitiesSharedPref;

    //botão para ir para a listagem de tipos de atividades
    private Button buttonListActivityTypes;

    //teste intent ao invés de shared pref

    private Intent intent;
    private Bundle args;
    private ArrayList<PlannedActivity> arrayListPlannedActivitiesIntent;
    private int extraValueMode;
    public static final String MODE = "MODE";
    public static final String EDIT_PLANNED_ACTIVITY = "EDIT_PLANNED_ACTIVITY";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    public static final int VIEW = 3;
    private String compareValue;
    private PlannedActivity plannedActivityEdit;
    private String trainingPlanIdIntent;

    //text view ao selecionar o tipo de atividades

    private TextView textViewStatisticType, textViewValueType;

    //recycler view e adapters
    private RecyclerView recyclerViewActivityDone;
    private AdapterRecyclerViewActivityDoneList adapterRecyclerViewActivityDoneList;
    private ArrayList<ActivityDone> arrayListActivityDone;
    private TextView textViewActivityDone;
    private Button buttonAddActivityDone;

    //textview de soma ou média de atividades feitas
    private TextView textViewSumOrAverage;
    private Double valueOfAllActivities = -1.00;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_planned_activities);


        //TESTE INTENT
        intent = getIntent();
        args = intent.getBundleExtra("BUNDLE");
        arrayListPlannedActivitiesIntent = (ArrayList<PlannedActivity>) args.getSerializable("ARRAYLIST");
        extraValueMode = getIntent().getExtras().getInt(MODE);
        trainingPlanIdIntent = getIntent().getExtras().getString(AddTrainingPlan.TRAINING_PLAN_ID);
        Log.i("TESTE", "TESTE");

        verifySignedInUser();

        if(auth != null) {
            userId = auth.getCurrentUser().getUid();
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        spinnerActivityType = findViewById(R.id.spinnerActivityTypePlanned);
        editTextGoal = findViewById(R.id.editTextGoalNumberAddPlanned);
        buttonListActivityTypes = findViewById(R.id.materialButtonListActivityTypes);
        editTextDescription = findViewById(R.id.editTextDescriptionPlannedActivity);
        populateSpinnerActivityType();

        //recycler view
        textViewActivityDone = findViewById(R.id.textViewActivityDonePlannedActivity);
        buttonAddActivityDone = findViewById(R.id.materialButtonAddActivityDonePlannedActivity);
        recyclerViewActivityDone = findViewById(R.id.recyclerViewAddedActivityDone);
        recyclerViewActivityDone.setHasFixedSize(true);
        recyclerViewActivityDone.setLayoutManager(new LinearLayoutManager(this));
        buttonAddActivityDone.setVisibility(View.GONE);
        recyclerViewActivityDone.setVisibility(View.GONE);
        textViewActivityDone.setVisibility(View.GONE);

        //text view de todas as atividades feitas

        textViewSumOrAverage = findViewById(R.id.textViewYourActivitiesFix);
        textViewSumOrAverage.setVisibility(View.GONE);


        //text view statistic e value type

        textViewStatisticType = findViewById(R.id.textViewStatisticTypeVar);
        textViewValueType = findViewById(R.id.textViewValueTypeVar);

        if(extraValueMode == NEW) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_planned_activity);
        }
        else if(extraValueMode == EDIT) {
            plannedActivityEdit = (PlannedActivity) getIntent().getSerializableExtra(EDIT_PLANNED_ACTIVITY);
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_planned_activity);
            editTextGoal.setText(String.valueOf(plannedActivityEdit.getGoal()));
            editTextDescription.setText(plannedActivityEdit.getDescription());
            spinnerActivityType.setEnabled(false);
            compareValue = plannedActivityEdit.getActivityTypeId();
            if(plannedActivityEdit.getPushId() != null) {
                populateRecyclerViewActivityDone();
                recyclerViewActivityDone.setVisibility(View.VISIBLE);
                textViewActivityDone.setVisibility(View.VISIBLE);
                buttonAddActivityDone.setVisibility(View.VISIBLE);
                textViewSumOrAverage.setVisibility(View.VISIBLE);
                buttonAddActivityDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AddPlannedActivity.this, ActivityDoneActivity.class);
                        intent.putExtra(MODE, NEW);
                        intent.putExtra(ActivityDoneActivity.PLANNED_ACTIVITY_ID, plannedActivityEdit.getPushId());
                        startActivity(intent);
                    }
                });
            }
            else {
                populateEditActivityTypeSpinner();
            }

        } else if(extraValueMode == VIEW) {
            plannedActivityEdit = (PlannedActivity) getIntent().getSerializableExtra(EDIT_PLANNED_ACTIVITY);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Planned Activity");
            editTextGoal.setText(String.valueOf(plannedActivityEdit.getGoal()));
            compareValue = plannedActivityEdit.getActivityTypeId();

            editTextGoal.setEnabled(false);
            spinnerActivityType.setEnabled(false);
            if(plannedActivityEdit.getPushId() != null) {
                populateRecyclerViewActivityDone();
                recyclerViewActivityDone.setVisibility(View.VISIBLE);
                textViewActivityDone.setVisibility(View.VISIBLE);
                buttonAddActivityDone.setVisibility(View.VISIBLE);
                textViewSumOrAverage.setVisibility(View.VISIBLE);
                buttonAddActivityDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AddPlannedActivity.this, ActivityDoneActivity.class);
                        intent.putExtra(MODE, NEW);
                        intent.putExtra(ActivityDoneActivity.PLANNED_ACTIVITY_ID, plannedActivityEdit.getPushId());
                        startActivity(intent);
                    }
                });
            }
            else {
                populateEditActivityTypeSpinner();
            }

        }



        spinnerActivityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ActivityType activityType = (ActivityType) spinnerAdapter.getItem(spinnerActivityType.getSelectedItemPosition());
                if(activityType.getStatisticsType().equals(StatisticsType.SUM)) {
                    textViewStatisticType.setText(R.string.sum);
                }
                else if(activityType.getStatisticsType().equals(StatisticsType.AVERAGE)){
                    textViewStatisticType.setText(R.string.average);
                }
                if(activityType.getValueType().equals(ValueTypes.NUMBERS)) {
                    textViewValueType.setText(R.string.numbers);
                    editTextGoal.setHint(R.string.numbers);
                }
                else if(activityType.getValueType().equals(ValueTypes.MINUTES)) {
                    textViewValueType.setText(R.string.minutes);
                    editTextGoal.setHint(R.string.minutes);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonListActivityTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToListActivityTypes();
            }
        });

    }


    public void populateSpinnerActivityType() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        userId = auth.getCurrentUser().getUid();
        spinnerActivityTypeArrayList = new ArrayList<ActivityType>();

        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                spinnerActivityTypeArrayList.clear();
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    ActivityType activityType = new ActivityType();
                    //Log.i("NAME: ", "CHEGOU");
                    activityType.setPushId(childSnapshot.getKey());
                    activityType.setName(childSnapshot.child("name").getValue(String.class));
                    activityType.setValueType(ValueTypes.valueOf(childSnapshot.child("valueType").getValue(String.class)));
                    activityType.setStatisticsType(StatisticsType.valueOf(childSnapshot.child("statisticsType").getValue(String.class)));
                    spinnerActivityTypeArrayList.add(activityType);


                }

                spinnerAdapter = new SpinnerAdapterActivityType(AddPlannedActivity.this, android.R.layout.simple_spinner_item, spinnerActivityTypeArrayList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerActivityType.setAdapter(spinnerAdapter);
                spinnerAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public void goToListActivityTypes() {
        startActivity(new Intent(this, ActivityTypeList.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_add_planned_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.buttonSavePlannedActivity) {//AddActivitiesToTrainingPlan();
            if(extraValueMode == NEW) {
                addPlannedActivityToTrainingPlan();
                return true;
            } else if(extraValueMode == EDIT) {

                editPlannedActivity();

            } else if(extraValueMode == VIEW) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(this);
                builder.setMessage(R.string.warning_save_training_plan);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editPlannedActivity();                                           }
                });
                builder.create();
                builder.show();
                return true;
            }

        }
        else if (itemId == android.R.id.home) {
            onBackPressed();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addPlannedActivityToTrainingPlan() {



        PlannedActivity plannedActivity = new PlannedActivity();
        String doubleString = editTextGoal.getText().toString();
        Double goal;
        String description = editTextDescription.getText().toString();
        if(doubleString.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.goal_not_set), Toast.LENGTH_SHORT).show();
            goal = 0.0;
            plannedActivity.setGoal(goal);
        } else {
            try {
                goal = Double.parseDouble(editTextGoal.getText().toString());
                plannedActivity.setGoal(goal);
            } catch(Exception e) {
                e.printStackTrace();
                Log.i("ERROR", "NÃO É DOUBLE");
            }

        }
        if(!description.isEmpty()) {
            if(trainingPlanIdIntent == null) {

                AlertDialog.Builder builder =  new AlertDialog.Builder(this);
                builder.setMessage(R.string.warning_cant_add_activities_done);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityType activityType = (ActivityType) spinnerAdapter.getItem(spinnerActivityType.getSelectedItemPosition());
                        plannedActivity.setActivityTypeId(activityType.getPushId());
                        plannedActivity.setActivityTypeName(activityType.getName());
                        plannedActivity.setDescription(description);

                        arrayListPlannedActivitiesIntent.add(plannedActivity);
                        Intent returnIntent = new Intent();
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable) arrayListPlannedActivitiesIntent);
                        returnIntent.putExtra("BUNDLE", args);
                        //returnIntent.putExtra("result",true);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                        Toast.makeText(getApplicationContext(), getString(R.string.added_planned_activity), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create();
                builder.show();


            } else {
                ActivityType activityType = (ActivityType) spinnerAdapter.getItem(spinnerActivityType.getSelectedItemPosition());
                plannedActivity.setActivityTypeId(activityType.getPushId());
                plannedActivity.setActivityTypeName(activityType.getName());
                plannedActivity.setDescription(description);
                plannedActivity.setUserId(userId);
                plannedActivity.setTrainingPlanId(trainingPlanIdIntent);
                String pushIdGenerated = plannedActivity.pushIdGenerated();
                plannedActivity.setPushId(pushIdGenerated);

                plannedActivity.saveToFirebaseWithPusId(pushIdGenerated);

                arrayListPlannedActivitiesIntent.add(plannedActivity);
                Intent returnIntent = new Intent();
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) arrayListPlannedActivitiesIntent);
                returnIntent.putExtra("BUNDLE", args);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.please_write_description, Toast.LENGTH_SHORT).show();
        }





    }

    public void editPlannedActivity() {
        PlannedActivity plannedActivity = new PlannedActivity();
        String doubleString = editTextGoal.getText().toString();
        Double goal;
        String description = editTextDescription.getText().toString();

        if(doubleString.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.goal_not_set), Toast.LENGTH_SHORT).show();
            goal = 0.0;
            plannedActivity.setGoal(goal);
        }
        else {
            try {
                goal = Double.parseDouble(editTextGoal.getText().toString());
                plannedActivity.setGoal(goal);
            } catch(Exception e) {
                e.printStackTrace();
                Log.i("ERROR", "NÃO É DOUBLE");
            }

        }
        if(!description.isEmpty()) {
            ActivityType activityType = (ActivityType) spinnerAdapter.getItem(spinnerActivityType.getSelectedItemPosition());
            plannedActivity.setActivityTypeId(activityType.getPushId());
            plannedActivity.setActivityTypeName(activityType.getName());
            plannedActivity.setDescription(description);
            plannedActivity.setUserId(userId);
            plannedActivity.setTrainingPlanId(plannedActivityEdit.getTrainingPlanId());
            plannedActivity.setPushId(plannedActivityEdit.getPushId());
            plannedActivity.updateValueToFirebase();

            for(int i = 0; i < arrayListPlannedActivitiesIntent.size(); i++) {
                PlannedActivity plannedActivityAux = arrayListPlannedActivitiesIntent.get(i);
                if(plannedActivityAux.getPushId().equals(plannedActivity.getPushId())) {
                    arrayListPlannedActivitiesIntent.set(i, plannedActivity);
                    Intent returnIntent = new Intent();
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", (Serializable) arrayListPlannedActivitiesIntent);
                    returnIntent.putExtra("BUNDLE", args);
                    //returnIntent.putExtra("result",true);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                }
            }


        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.please_write_description), Toast.LENGTH_SHORT).show();
        }


    }

    public void populateEditActivityTypeSpinner() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        spinnerActivityTypeArrayList = new ArrayList<>();

        firebase.child("activityType").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                spinnerActivityTypeArrayList.clear();
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ActivityType activityType = childSnapshot.getValue(ActivityType.class);
                    activityType.setPushId(childSnapshot.getKey());
                    activityType.setName(childSnapshot.child("name").getValue(String.class));
                    activityType.setValueType(ValueTypes.valueOf(childSnapshot.child("valueType").getValue(String.class)));
                    spinnerActivityTypeArrayList.add(activityType);
                }
                spinnerAdapter = new SpinnerAdapterActivityType(AddPlannedActivity.this, android.R.layout.simple_spinner_item, spinnerActivityTypeArrayList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerActivityType.setAdapter(spinnerAdapter);

                if(spinnerAdapter.getCount() > 0 ) {
                    for(int i = 0; i < spinnerAdapter.getCount(); i++) {
                        ActivityType activityType = (ActivityType) spinnerAdapter.getItem(i);
                        if(activityType.getPushId().equals(compareValue)){
                            if(valueOfAllActivities > 0) {
                                if(activityType.getStatisticsType().equals(StatisticsType.AVERAGE)) {
                                    valueOfAllActivities = valueOfAllActivities / adapterRecyclerViewActivityDoneList.getItemCount();
                                    String activityDoneAverage;
                                    DecimalFormat numberFormat = new DecimalFormat("#0.00");
                                    numberFormat.setRoundingMode(RoundingMode.DOWN);
                                    if(activityType.getValueType().equals(ValueTypes.MINUTES)) {
                                        activityDoneAverage = convertToTime(valueOfAllActivities);
                                    }
                                    else {
                                        activityDoneAverage = numberFormat.format(valueOfAllActivities);
                                    }


                                    double goalPercentage = (valueOfAllActivities/plannedActivityEdit.getGoal())*100;
                                    String goalPercentageString = numberFormat.format(goalPercentage) + "%)";
                                    SpannableString ss = new SpannableString(goalPercentageString);
                                    String stringAverageFormat = getResources().getString(R.string.goal_percentage_done_average);
                                    String stringAverageMsg = String.format(stringAverageFormat, activityDoneAverage);
                                    if(goalPercentage >= 100) {
                                        ss.setSpan(new ForegroundColorSpan(Color.GREEN), 0, goalPercentageString.length() - 1, 0);
                                    }
                                    else {
                                        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, goalPercentageString.length() - 1, 0);
                                    }
                                    textViewSumOrAverage.setText("");
                                    textViewSumOrAverage.append(stringAverageMsg);
                                    textViewSumOrAverage.append(ss);


                                }
                                else if(activityType.getStatisticsType().equals(StatisticsType.SUM)) {
                                    String activityDoneSum;
                                    DecimalFormat numberFormat = new DecimalFormat("#0.00");
                                    numberFormat.setRoundingMode(RoundingMode.DOWN);
                                    if(activityType.getValueType().equals(ValueTypes.MINUTES)) {
                                        activityDoneSum = convertToTime(valueOfAllActivities);
                                    }
                                    else {
                                        activityDoneSum = numberFormat.format(valueOfAllActivities);
                                    }
                                    double goalPercentage = (valueOfAllActivities/plannedActivityEdit.getGoal())*100;
                                    String goalPercentageString = numberFormat.format(goalPercentage) + "%)";
                                    SpannableString ss = new SpannableString(goalPercentageString);
                                    String stringSumFormat = getResources().getString(R.string.goal_percentage_done_sum);
                                    String stringSumMsg = String.format(stringSumFormat, activityDoneSum);
                                    if(goalPercentage >= 100) {
                                        ss.setSpan(new ForegroundColorSpan(Color.GREEN), 0, goalPercentageString.length() - 1, 0);
                                    }
                                    else {
                                        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, goalPercentageString.length() - 1, 0);
                                    }

                                    textViewSumOrAverage.setText("");
                                    textViewSumOrAverage.append(stringSumMsg);
                                    textViewSumOrAverage.append(ss);


                                }
                            }


                            spinnerActivityType.setSelection(i);
                            break;
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void populateRecyclerViewActivityDone() {
        DatabaseReference firebase = FirebaseConfig.getFirebaseDatabase();
        firebase.child("activityDone").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                valueOfAllActivities = 0.0;
                arrayListActivityDone = new ArrayList<>();
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ActivityDone activityDone = childSnapshot.getValue(ActivityDone.class);
                    activityDone.setUserId(userId);
                    activityDone.setActivityTypeAuxId(plannedActivityEdit.getActivityTypeId());
                    activityDone.setPushId(childSnapshot.getKey());
                    if(plannedActivityEdit.getPushId().equals(activityDone.getPlannedActivityId())) {
                        valueOfAllActivities += activityDone.getValue();
                        arrayListActivityDone.add(activityDone);
                    }
                }
                adapterRecyclerViewActivityDoneList = new AdapterRecyclerViewActivityDoneList(AddPlannedActivity.this, arrayListActivityDone);
                recyclerViewActivityDone.setAdapter(adapterRecyclerViewActivityDoneList);
                populateEditActivityTypeSpinner();
                if(arrayListActivityDone.isEmpty()) {
                    textViewSumOrAverage.setVisibility(View.GONE);
                }
                else {
                    textViewSumOrAverage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String convertToTime(double value) {
        int totalMinutes = (int) value;
        int hour = totalMinutes/60;
        int minutes = totalMinutes % 60;
        return hour + getString(R.string.h_hours) + minutes + getString(R.string.m_minutes);
    }

}