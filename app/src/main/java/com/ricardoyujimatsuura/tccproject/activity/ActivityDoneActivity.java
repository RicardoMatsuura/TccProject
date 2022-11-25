package com.ricardoyujimatsuura.tccproject.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ricardoyujimatsuura.tccproject.R;
import com.ricardoyujimatsuura.tccproject.config.FirebaseConfig;
import com.ricardoyujimatsuura.tccproject.models.ActivityDone;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class ActivityDoneActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String userId;

    public static final String MODE = "MODE";
    public static final String PLANNED_ACTIVITY_ID = "PLANNED_ACTIVITY_ID";
    public static final String EDIT_ACTIVITY_DONE = "EDIT_ACTIVITY_DONE";
    public static final int NEW = 1;
    public static final int EDIT = 2;
    private int extraValueMode;

    //edit text

    private EditText editTextValue, editTextNote;
    private String plannedActivityIdIntent;
    private ActivityDone activityDoneIntentEdit;

    //time e date
    private TextView textViewDate, textViewTime;
    private Button buttonDate, buttonTime;
    private int hour, minute, year, month, day;
    private Calendar combinedCalendar;
    private Long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        verifySignedInUser();
        extraValueMode = getIntent().getExtras().getInt(MODE);
        editTextValue = findViewById(R.id.editTextActivityDoneValue);
        editTextNote = findViewById(R.id.editTextNote);
        combinedCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

        plannedActivityIdIntent = getIntent().getExtras().getString(PLANNED_ACTIVITY_ID);

        //time e data

        textViewDate = findViewById(R.id.textViewDateDone);
        textViewTime = findViewById(R.id.textViewTimeDone);
        buttonDate = findViewById(R.id.materialButtonDatePick);
        buttonTime = findViewById(R.id.materialButtonTimePick);

        if(auth != null) {
            userId = auth.getCurrentUser().getUid();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(extraValueMode == NEW) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
            textViewTime.setText(String.format(Locale.getDefault(), getString(R.string.hour_minute_variable), hour, minute));
            textViewDate.setText(makeDateString(day,month + 1,year));
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_activity_done);
        } else if(extraValueMode == EDIT) {

            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_activity_done);
            activityDoneIntentEdit = (ActivityDone) getIntent().getSerializableExtra(EDIT_ACTIVITY_DONE);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(activityDoneIntentEdit.getTimestamp());
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
            textViewTime.setText(String.format(Locale.getDefault(), getString(R.string.hour_minute_variable), hour, minute));
            textViewDate.setText(makeDateString(day,month + 1,year));
            editTextValue.setText(String.valueOf(activityDoneIntentEdit.getValue()));
            editTextNote.setText(activityDoneIntentEdit.getNote());
        }
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        hour = selectedHour;
                        minute = selectedMinute;
                        textViewTime.setText(String.format(Locale.getDefault(), getString(R.string.hour_minute_variable), hour, minute));
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(ActivityDoneActivity.this, R.style.DialogTheme, onTimeSetListener, hour, minute, true);
                timePickerDialog.setTitle(getString(R.string.select_time));
                timePickerDialog.show();
            }
        });

        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        year = selectedYear;
                        month = selectedMonth;
                        day = selectedDay;
                        selectedMonth = selectedMonth +1;
                        String date = makeDateString(selectedDay,selectedMonth,selectedYear);
                        textViewDate.setText(date);
                        combinedCalendar.set(year, month, day);
                    }
                };

                    DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityDoneActivity.this, R.style.DialogTheme, onDateSetListener, year, month, day);
                    datePickerDialog.setTitle(getString(R.string.select_date));
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePickerDialog.show();
            }
        });


    }

    public String makeDateString(int day, int month, int year) {
        return day + "/" + month + "/" + year;
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
        getMenuInflater().inflate(R.menu.options_menu_activity_done, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.menuItemSaveActivityDone) {
            if(extraValueMode == NEW) {
                ActivityDone activityDone = new ActivityDone();
                String note = editTextNote.getText().toString();
                Double value;
                try {
                    value = Double.parseDouble(editTextValue.getText().toString());
                    activityDone.setValue(value);
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.i("ERROR", "NÃO É DOUBLE");
                }

                combinedCalendar.set(year, month, day);
                combinedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                combinedCalendar.set(Calendar.MINUTE, minute);
                timestamp = combinedCalendar.getTimeInMillis();

                activityDone.setPlannedActivityId(plannedActivityIdIntent);
                activityDone.setUserId(userId);
                activityDone.setNote(note);
                activityDone.setTimestamp(timestamp);
                activityDone.saveToFirebase();
                finish();
                Toast.makeText(getApplicationContext(), R.string.activity_done_added, Toast.LENGTH_SHORT).show();

            } else if(extraValueMode == EDIT) {

                ActivityDone activityDone = new ActivityDone();
                String note = editTextNote.getText().toString();
                Double value;
                try {
                    value = Double.parseDouble(editTextValue.getText().toString());
                    activityDone.setValue(value);
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.i("ERROR", "NÃO É DOUBLE");
                }
                activityDone.setPlannedActivityId(activityDoneIntentEdit.getPlannedActivityId());
                activityDone.setPushId(activityDoneIntentEdit.getPushId());
                activityDone.setUserId(userId);
                activityDone.setNote(note);
                combinedCalendar.set(year, month, day);
                combinedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                combinedCalendar.set(Calendar.MINUTE, minute);
                timestamp = combinedCalendar.getTimeInMillis();
                activityDone.setTimestamp(timestamp);
                activityDone.updateToFirebase();
                finish();
                Toast.makeText(getApplicationContext(), R.string.activity_done_edited, Toast.LENGTH_SHORT).show();

            }

        }
        else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}