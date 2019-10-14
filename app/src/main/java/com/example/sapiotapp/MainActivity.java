package com.example.sapiotapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private int heart_rate;
    private boolean heart_rate_low;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this is a workaround to allow the app to perform a timer in the main GUI process
        //(app freezes for a little while )
        //its recommended to kick this two statements out and place a timer in a background process
        createNotificationChannel();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Keep the Wear screen always on (for testing only!)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //a simple button to start and stop the timer for sending the sensor data to the Cloud platform
        Button b = (Button) findViewById(R.id.button);
        b.setText("start");
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("stop")) {
                    timerHandler.removeCallbacks(timerRunnable);
                    b.setText("start");
                } else {
                    timerHandler.postDelayed(timerRunnable, 0);
                    b.setText("stop");
                }
            }
        });
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heart_rate = (int) event.values[0];
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            if (heart_rate != 0) {
                if (heart_rate < 80) {
                    heart_rate_low = true;
                    sendAlert();
                } else if (heart_rate > 80) {
                    heart_rate_low = false;
                    sendAlert();
                }

                //sendNotification();

            } else {
                System.out.println("Everything seems fine.");

            }
            timerHandler.postDelayed(this, 3000);
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        CharSequence name = "Heart";
        String description ="Heart Rate";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("my_channel_01", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        System.out.println("Success channel");
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void sendAlert() {
        /**
         //INSERT HERE THE VALUES of your IoT service, which can be found in the IoT Service Cockpit
         **/
        String DEVICE_ALTERNATE_ID = "4cd0f5ad-9e47-4709-86ac-3051c036b85e";
        String SENSOR_ALTERNATE_ID = "e6ab678c-7d79-4030-85ef-08f7eae6d794";
        String TENANT = "1cf82f59-f178-4c3a-9b37-04ad4c472ede.eu10.cp.iot.sap";
        String CAPABILITY_ALTERNATE_ID = "a40a3786-651c-40f8-92f9-4fee2ba01d1f";
        try {
            //Parameterised Url and post request to send to the Cloud platform
            String START = "https://";
            String MAIN = "/iot/gateway/rest/measures/";
            String POST_ADDRESS = (START + TENANT + MAIN + DEVICE_ALTERNATE_ID);
            URL urlAddress = new URL(POST_ADDRESS);
            JSONObject stepCountJson = new JSONObject();
            if (heart_rate_low) {
                stepCountJson.put("Message", "Heart rate is too low!");
            } else {
                stepCountJson.put("Message", "Heart rate is too high!");
            }
            //
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(stepCountJson);
            // jsonArray.put(stepHeart);
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("capabilityAlternateId", CAPABILITY_ALTERNATE_ID);
            bodyJson.put("sensorAlternateId", SENSOR_ALTERNATE_ID);
            bodyJson.put("measures", jsonArray);
            String SENSOR_DATA = bodyJson.toString();
            System.out.println(SENSOR_DATA);
            // Embedding Certificate
            InputStream resourceStream = getResources().openRawResource(R.raw.certificate);
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(resourceStream, "12345678".toCharArray());
            System.out.println("Loaded certificates: " + keyStore.size());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "12345678".toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            HttpsURLConnection connection  = (HttpsURLConnection) urlAddress.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(1000);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setInstanceFollowRedirects(false);
            //Sending the message
            byte[] bytes = SENSOR_DATA.getBytes("UTF_8");
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            int status = connection.getResponseCode();
            System.out.println("Response status: " + String.valueOf(status));
        } catch (Exception ex) {
            //error handling
            System.out.println("ERROR " + ex.getMessage());
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button b = (Button)findViewById(R.id.button);
        b.setText("start");
    }

}