package com.example.attaurrahman.task;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Created by kashif on 1/10/18.
 */

public class Utilities {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;



    public static SharedPreferences.Editor putValueInEditor(Context context) {
        sharedPreferences = getSharedPreferences(context);
        editor = sharedPreferences.edit();
        return editor;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        //sharedPreferences = context.getSharedPreferences(Configuration.MY_PREF, 0);
        return context.getSharedPreferences(Configurations.MY_PREF, 0);
    }



    private static class Configurations {
        public static final String MY_PREF = "My_Pref";
    }



    }




