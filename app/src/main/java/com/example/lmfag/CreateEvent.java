package com.example.lmfag;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {

    final Calendar cldr = Calendar.getInstance();
    int hour = cldr.get(Calendar.HOUR_OF_DAY);
    int minutes = cldr.get(Calendar.MINUTE);
    int year = cldr.get(Calendar.YEAR);
    int month = cldr.get(Calendar.MONTH);
    int day = cldr.get(Calendar.DAY_OF_MONTH);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Spinner sp = findViewById(R.id.sp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        ImageView floatingActionButtonChooseTime = findViewById(R.id.floatingActionButtonChooseTime);
        TextView textViewChooseTime = findViewById(R.id.textViewChooseTime);
        Context context = this;
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
        ImageView floatingActionButtonChooseDate = findViewById(R.id.floatingActionButtonChooseDate);
        TextView textViewChooseDate = findViewById(R.id.textViewChooseDate);
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

}