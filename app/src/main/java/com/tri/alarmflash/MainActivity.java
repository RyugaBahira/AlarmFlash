package com.tri.alarmflash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity
{
    private static boolean toggleON = false;
    TimePicker alarmTimePicker;
    static PendingIntent pendingIntent; //harus static biar close app ttp ada
    static AlarmManager alarmManager; //harus static biar close app ttp ada
    public static boolean hasCameraFlash = false; //harus static biar close app ttp ada
    private final int CAMERA_REQUEST = 123;
    private ToggleButton togglebtn;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmTimePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        togglebtn = (ToggleButton) findViewById(R.id.toggleButton);
        togglebtn.setChecked(MainActivity.toggleON);//klo app dclose trus masuk lg, load state trakhir

        //camera termasuk runtime permission di android 6 ke atas
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


    }
    public void OnToggleClicked(View view)
    {
        long time;
        MainActivity.toggleON = ((ToggleButton) view).isChecked();
        if (MainActivity.toggleON)
        {
            Toast.makeText(MainActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            time=(calendar.getTimeInMillis()-(calendar.getTimeInMillis()%60000));
            if(System.currentTimeMillis()>time)
            {
                if (calendar.AM_PM == 0)
                    time = time + (1000*60*60*12);
                else
                    time = time + (1000*60*60*24);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 30000, pendingIntent);
//setrepeating ga exact di android marshmallow ke atas, bs jadi set per 10dtk tp jadinya didelay smpe 1mnt
            //di android marshmallow ke atas minimum 30dtk interval

        }
        else
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Toast.makeText(MainActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();

        }
    }
}
