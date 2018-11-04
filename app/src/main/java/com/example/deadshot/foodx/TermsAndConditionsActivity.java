package com.example.deadshot.foodx;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.security.AllPermission;
import java.util.ArrayList;
import java.util.List;

public class TermsAndConditionsActivity extends AppCompatActivity {

    public static final String TAG = "TermsAndConditionsActivity->>";

    private Context context = null;

    private CheckBox terms_checkBox = null;
    public static boolean allPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        context = getApplicationContext();

        terms_checkBox = findViewById(R.id.Terms_checkBox);


        final Activity activity = this;
        terms_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (terms_checkBox.isChecked()){
                    getAllPermissions(activity);
                }
            }
        });

    }
    @SuppressLint("MissingPermission")
    public void GetStartedButton_click(View v){
        if (!terms_checkBox.isChecked()){
            Toast.makeText(getApplicationContext(), "Please Agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getAllPermissions(this)){
            TelephonyManager mTelephonyMgr;
            mTelephonyMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

            String myNumber = mTelephonyMgr.getLine1Number();
            Intent i =new Intent(getApplicationContext(),SignInScreen.class);
            i.putExtra("Phone_Number",myNumber.toString());
            startActivity(i);
        }
        else{
            Toast.makeText(getApplicationContext(), "Kindly give all the permissions Required", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            Log.d("Actual Permissions",((Integer)permissions.length).toString());
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    Log.d(TAG,"hasPermissions: "+permission.toString()+" not granted");
                    return false;
                }
            }
        }
        allPermissionsGranted = true;
        Log.d(TAG,"hasPermissions: all permissions granted");
        return true;
    }
    public static Boolean getAllPermissions(Activity activity){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
        };

        if(!hasPermissions(activity.getApplicationContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
        return hasPermissions(activity.getApplicationContext(),PERMISSIONS);
    }

}
