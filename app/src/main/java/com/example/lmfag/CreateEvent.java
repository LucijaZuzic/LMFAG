package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEvent extends AppCompatActivity {

    final Calendar cldr = Calendar.getInstance();
    int hour = cldr.get(Calendar.HOUR_OF_DAY);
    int minutes = cldr.get(Calendar.MINUTE);
    int year = cldr.get(Calendar.YEAR);
    int month = cldr.get(Calendar.MONTH);
    int day = cldr.get(Calendar.DAY_OF_MONTH);
    double longitude = 45.23;
    double latitude = 45.36;
    ImageView floatingActionButtonChooseDate = findViewById(R.id.floatingActionButtonChooseDate);
    TextView textViewChooseDate = findViewById(R.id.textViewChooseDate);
    ImageView floatingActionButtonChooseTime = findViewById(R.id.floatingActionButtonChooseTime);
    TextView textViewChooseTime = findViewById(R.id.textViewChooseTime);
    ImageView apply = findViewById(R.id.imageViewApply);
    Spinner sp = findViewById(R.id.sp);
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        fillSpinner();
        setDate();
        setTime();
    }
    void setDate() {
        floatingActionButtonChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // time picker dialog
                DatePickerDialog picker = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker dp, int sYear, int sMonth, int sDay) {
                                day = sDay;
                                month = sMonth;
                                year = sYear;
                                textViewChooseDate.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

    }
    void setTime() {
        floatingActionButtonChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                hour = sHour;
                                minutes = sMinute;
                                textViewChooseTime.setText(hour + ":" + minutes);
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });

    }
    void fillSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    void writeDB(Map<String, Object> docData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .add(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    Snackbar.make(apply, R.string.write_success, Snackbar.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Snackbar.make(apply, R.string.logged_in, Snackbar.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userID", aVoid.getId());
                    editor.apply();
                    Intent myIntent = new Intent(context, MyProfile.class);
                    startActivity(myIntent);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }

}