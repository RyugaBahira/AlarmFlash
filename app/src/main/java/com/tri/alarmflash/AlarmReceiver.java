package com.tri.alarmflash;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    private Camera camera;
    private boolean isFlashOn = false;
    private Camera.Parameters params;
    private CameraManager cameraManager;

    ////////////////////////////////////////
    //camera api < 21
    private void blink(final int delay, final int times) {
        Thread t = new Thread() {
            public void run() {
                try {
                    if (camera == null || params == null) {
                        camera = Camera.open();
                        try {
                            camera.setPreviewDisplay(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        camera.startPreview();
                    }

                    for (int i=0; i < times*2; i++) {
                        if (isFlashOn) {
                            turnOffFlash();
                        } else {
                            turnOnFlash();
                        }
                        sleep(delay);
                    }

                    if (camera != null) {
                        camera.stopPreview();
                        camera.release();
                        camera = null;

                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void turnOnFlash() {
        if (!isFlashOn) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            isFlashOn = true;
        }
        MainActivity.imgbulb.setImageResource(R.drawable.bulb_on);
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            isFlashOn = false;
        }
        MainActivity.imgbulb.setImageResource(R.drawable.bulb_off);
    }
/////////////////////////////////////////////////
    //camera api >= 21 (lollipop)
    @SuppressLint("NewApi")
    private void flashLightOn(Context ctx) {
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
        }
        MainActivity.imgbulb.setImageResource(R.drawable.bulb_on);
    }

    @SuppressLint("NewApi")
    private void flashLightOff(Context ctx) {
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
        }
        MainActivity.imgbulb.setImageResource(R.drawable.bulb_off);
    }

    @SuppressLint("NewApi")
    private void blinkFlash(Context ctx) {
        cameraManager = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
        String myString = "0101010101";
        long blinkDelay = 50; //Delay in ms
        for (int i = 0; i < myString.length(); i++) {
            if (myString.charAt(i) == '0') {
                flashLightOn(ctx);
            } else {
                flashLightOff(ctx);
            }
            try {
                Thread.sleep(blinkDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show();

        Uri alarmUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.labbaik_allahumma_labbaik_mishari);

//        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//        if (alarmUri == null)
//        {
//            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();

        if (MainActivity.hasCameraFlash) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                blinkFlash(context);
            else
                blink(50, 10);
        }
    }
}
