package com.example.attaurrahman.task;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.taishi.library.Indicator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.log10;

public class MainActivity extends AppCompatActivity {
    TextView mStatusView, tvNoiseDetector, tvLocation, tvTimeStamp;
    MediaRecorder mRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    String strNoise;
    String strLatLon;
    String strTime;
    File rootFile, CsvFile;
    FileWriter writer;
    Boolean aBoolean;
    double lattitude;
    double longitude;
    LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    Typeface typeface, typeface2;
    Indicator indicator;
    int indicatorstepnum = 0;
    int timer_int ;
    int spinner_index;

    MaterialSpinner spinner;

    private static MainActivity instance;

    boolean aBooleanIsStarted = true;
    Button btnReset;
    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    boolean aBooleanFirst,aBooleanSecond;




    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();

        }
    };

    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        clearApplicationData();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        timer_int = Utilities.getSharedPreferences(MainActivity.this).getInt("timer", 1000);

        aBooleanIsStarted = Utilities.getSharedPreferences(MainActivity.this).getBoolean("isStarted", true);

        if (aBooleanIsStarted) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            } else {

                startRecorder();


            }
        }


        mStatusView = findViewById(R.id.status);
        tvNoiseDetector = findViewById(R.id.tv_noise_detector);
        tvLocation = findViewById(R.id.tv_location);
        tvTimeStamp = findViewById(R.id.tv_time_stamp);
        indicator = findViewById(R.id.indicator);


        spinner = findViewById(R.id.spinner);
        spinner.setItems("1 Second", "5 Second", "1 Minute", "5 Minute", "10 Minute", "20 Minute", "30 Minute");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

                if (item == "1 Second") {
                    timer_int = 1000;
                } else if (item == "5 Second") {
                    timer_int = 5000;
                } else if (item == "1 Minute") {
                    timer_int = 60000;
                } else if (item == "5 Minute") {
                    timer_int = 300000;
                } else if (item == "10 Minute") {
                    timer_int = 600000;
                } else if (item == "20 Minute") {
                    timer_int = (int) 1.2e+6;
                } else if (item == "30 Minute") {
                    timer_int = (int) 1.8e+6;
                }
                Utilities.putValueInEditor(MainActivity.this).putInt("timer", timer_int).commit();
                Utilities.putValueInEditor(MainActivity.this).putInt("spinner_index_value", position).commit();
            }
        });

        spinner_index = Utilities.getSharedPreferences(MainActivity.this).getInt("spinner_index_value", 0);
        spinner.setSelectedIndex(spinner_index);


        typeface = Typeface.createFromAsset(this.getAssets(), "billabong.ttf");
        typeface2 = Typeface.createFromAsset(this.getAssets(), "SanFrancisco.otf");
        tvTimeStamp.setTypeface(typeface2);
        tvLocation.setTypeface(typeface2);
        mStatusView.setTypeface(typeface2);
        tvNoiseDetector.setTypeface(typeface2);
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(timer_int);
                            Log.i("Noise", "Tock");
                        } catch (InterruptedException e) {
                        }
                        ;
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        Utilities.putValueInEditor(MainActivity.this).putBoolean("isStarted", false).commit();


    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try {


                mRecorder.start();

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateTv() {


     //   Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();

        aBooleanFirst = true;
        aBooleanSecond = false;
        if (aBooleanFirst){

        }
        if (tvNoiseDetector.getText().equals("-∞ dBA")){
            startRecorder();
        }



        double dNoise = soundDb(1.1);
        DecimalFormat decimalFormat = new DecimalFormat("####.##");
        String strNoiseDF = decimalFormat.format(dNoise);
        strNoise = strNoiseDF;
        mStatusView.setText(strNoise+" dBA");

        Log.d("zma str noise", String.valueOf(getAmplitudeEMA()));


        ////this code use for  indicator
        indicatorstepnum = mStatusView.getText().length();
        int i = 0;
        i = indicatorstepnum / 10;
        indicator.setBarNum(50);
        indicator.setStepNum((int) getAmplitude());
        indicator.setDuration(100);

///get Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show();


        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
            generateNoteOnSD();

        }


    }


    public double soundDb(double ampl) {
        return 20 * log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }


    public void generateNoteOnSD() {


        try {

            rootFile = new File(Environment.getExternalStorageDirectory(), "Noise Detector");

            if (!rootFile.exists()) {
                rootFile.mkdirs();
                Utilities.putValueInEditor(this).putBoolean("title", true).commit();
            }
            CsvFile = new File(rootFile, "Noise Detector" + ".CSV");
            if (!CsvFile.exists()) {
                Utilities.putValueInEditor(this).putBoolean("title", true).commit();
            }
            writer = new FileWriter(CsvFile, true);
            aBoolean = Utilities.getSharedPreferences(this).getBoolean("title", false);
            if (aBoolean) {
                writer.append("Noise dBA");
                writer.append(COMMA_DELIMITER);
                writer.append("Location");
                writer.append(COMMA_DELIMITER);
                writer.append(COMMA_DELIMITER);
                writer.append("Time Stamp");
                writer.append(NEW_LINE_SEPARATOR);
            }
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(c.getTime());
            strTime = String.valueOf(formattedDate);

            strLatLon = String.valueOf(lattitude + "," + longitude);


            writer.append(strNoise);
            writer.append(COMMA_DELIMITER);
            writer.append(strLatLon);
            writer.append(COMMA_DELIMITER);
            writer.append(strTime);
            writer.append(NEW_LINE_SEPARATOR);


            writer.flush();
            writer.close();

            tvLocation.setText(strLatLon);
            tvTimeStamp.setText(strTime);

            // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();


            Utilities.putValueInEditor(this).putBoolean("title", false).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();

                lattitude = latti;
                longitude = longi;


            } else if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();

                lattitude = latti;
                longitude = longi;


            } else if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                lattitude = latti;
                longitude = longi;


            } else {

                Toast.makeText(this, "Unable to Trace your location", Toast.LENGTH_SHORT).show();

            }


        }
    }


public static MainActivity getInstance(){
        return instance;
}
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s +" DELETED ");
                }
            }
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }


}

