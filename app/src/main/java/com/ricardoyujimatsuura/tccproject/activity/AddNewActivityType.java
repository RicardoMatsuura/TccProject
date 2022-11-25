package com.ricardoyujimatsuura.tccproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.adapter.SpinnerAdapterStatisticType;
import com.ricardoyujimatsuura.tccproject.adapter.SpinnerAdapterValueType;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.enums.StatisticsType;
import com.ricardoyujimatsuura.tccproject.enums.ValueTypes;
import com.ricardoyujimatsuura.tccproject.models.ActivityType;

import java.util.Objects;

public class AddNewActivityType extends AppCompatActivity {

    private FirebaseAuth auth;
    private String userId;

    private EditText editTextActivityTypeName, editTextActivityTypeDescription;
    private TextView textViewEditName, textViewEditDescription, textViewStatisticInfo;


    private ActivityType activityType;


    //intent extras

    public static final String MODE = "MODE";
    public static final String EDIT_ACTIVITY_TYPE = "EDIT_ACTIVITY_TYPE";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    private int extraValueMode;
    private ActivityType activityTypeEdit;


    //spinner value types
    private ArrayAdapter<ValueTypes> adapterValueType;
    private Spinner spinnerValueType;

    //spinner statistic type
    private Spinner spinnerStatisticType;
    private ArrayAdapter<StatisticsType> adapterStatisticType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_activities_type);

        verifySignedInUser();

        //botão back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //titulo
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_activity_type);

        //modo de edição ou novo
        extraValueMode = getIntent().getExtras().getInt(MODE);

        editTextActivityTypeName = findViewById(R.id.editTextActivityTypeName);
        editTextActivityTypeDescription = findViewById(R.id.editTextActivityTypeDescription);
        spinnerValueType = findViewById(R.id.spinnerNewGameActivityValueType);
        spinnerStatisticType = findViewById(R.id.spinnerNewGameActivityStatisticType);
        textViewEditDescription = findViewById(R.id.textViewEditDescriptionActivityType);
        textViewEditName = findViewById(R.id.textViewEditNameActivityType);
        textViewStatisticInfo = findViewById(R.id.textViewStatisticInfo);


        if(auth != null) {
            userId = auth.getCurrentUser().getUid();
            populateStatisticTypeSpinner();
            populateValueTypeSpinner();
        }

        if(extraValueMode == EDIT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.edit_activity_type));
            activityTypeEdit = (ActivityType) getIntent().getSerializableExtra(EDIT_ACTIVITY_TYPE);

            editTextActivityTypeDescription.setText(activityTypeEdit.getDescription());
            editTextActivityTypeName.setText(activityTypeEdit.getName());
            int spinnerValueTypePosition = adapterValueType.getPosition(activityTypeEdit.getValueType());
            spinnerValueType.setSelection(spinnerValueTypePosition);
            int spinnerStatisticTypePosition = adapterStatisticType.getPosition(activityTypeEdit.getStatisticsType());
            spinnerStatisticType.setSelection(spinnerStatisticTypePosition);

        }
        else if(extraValueMode == NEW) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_activity_type);
        }

        spinnerStatisticType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(spinnerStatisticType.getSelectedItem().equals(StatisticsType.SUM)) {
                    textViewStatisticInfo.setText(getString(R.string.sum_explanation));
                }
                else if(spinnerStatisticType.getSelectedItem().equals(StatisticsType.AVERAGE)) {
                    textViewStatisticInfo.setText(getString(R.string.average_explanation));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public void populateValueTypeSpinner() {

        adapterValueType = new SpinnerAdapterValueType(this, android.R.layout.simple_spinner_item);
        adapterValueType.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        spinnerValueType.setAdapter(adapterValueType);

    }

    public void populateStatisticTypeSpinner() {
        adapterStatisticType = new SpinnerAdapterStatisticType(this,android.R.layout.simple_spinner_item);
        adapterStatisticType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatisticType.setAdapter(adapterStatisticType);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_add_new_activity_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.buttonSaveNewActivityTypeContextual) {
            if(extraValueMode == NEW) {
                saveNewActivityType();
            }
            else if (extraValueMode == EDIT) {
                updateActivityType();
            }

            return true;
        } else if (itemId == android.R.id.home) {
            onBackPressed();
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);

    }


    public void saveNewActivityType() {


        String name = editTextActivityTypeName.getText().toString();
        String description = editTextActivityTypeDescription.getText().toString();

        ValueTypes valueType = (ValueTypes) spinnerValueType.getSelectedItem();
        StatisticsType statisticType = (StatisticsType) spinnerStatisticType.getSelectedItem();
        activityType = new ActivityType();
        activityType.setName(name);
        activityType.setDescription(description);
        activityType.setStatisticsType(statisticType);
        activityType.setValueType(valueType);
        activityType.setUserId(userId);
        if(!name.isEmpty()) {
            if(!description.isEmpty()) {
                activityType.saveToFirebase();
                finish();
                Toast.makeText(getApplicationContext(), getString(R.string.new_activity_type_added), Toast.LENGTH_LONG).show();

            }
        }

    }

    public void updateActivityType() {
        String name = editTextActivityTypeName.getText().toString();
        String description = editTextActivityTypeDescription.getText().toString();
        ValueTypes valueType = (ValueTypes) spinnerValueType.getSelectedItem();
        StatisticsType statisticsType = (StatisticsType) spinnerStatisticType.getSelectedItem();

        ActivityType editActivityType = new ActivityType();
        editActivityType.setUserId(userId);
        editActivityType.setPushId(activityTypeEdit.getPushId());
        editActivityType.setName(name);
        editActivityType.setDescription(description);
        editActivityType.setValueType(valueType);
        editActivityType.setStatisticsType(statisticsType);

        if(!name.isEmpty()) {
            if(!description.isEmpty()) {
                editActivityType.updateToFirebase();
                finish();
                Toast.makeText(getApplicationContext(), getString(R.string.activity_type_updated), Toast.LENGTH_LONG).show();

            }
        }

    }
}