package com.example.deadshot.foodx;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignInScreen extends AppCompatActivity {

    private CountryCodePicker ccp;
    private Button checkNumButton;
    private String mTo;
    private Random rand = new Random();
    private String mBody = String.valueOf(rand.nextInt(999999) + 100000);   //This will generate the 6 digit verification code
    private OkHttpClient mClient = new OkHttpClient();
    private Context mContext;

    //This function is called to send the verification
    // SMS where "num" is the number on which the msg
    // is sent and "mBody" is the verification code
    Call post(String url, Callback callback) {
        String num = mTo.toString();
        RequestBody formBody = new FormBody.Builder().add("To", num).add("Body", mBody).build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call response = mClient.newCall(request);
        response.enqueue(callback);
        return response;
    }
    @SuppressLint("LongLogTag")
    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.d("Hash Key: ", hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.d("printHashKey()", e.toString());
        } catch (Exception e) {
            Log.d("printHashKey()", e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);

        //Needed for sending Verification SMS
        mContext = getApplicationContext();

        printHashKey(mContext);
        //Getting all the permissions required
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);

        //Getting the number of the user if possible i.e., if SIM shares it with us
        String number = getMyPhoneNO();

        //Initializing the Country Picker
        ccp = findViewById(R.id.ccp);
        final EditText editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);

        //Setting the Edit Text to be associated with the country picker so that country picker can get the phone number as well as the country code
        ccp.registerCarrierNumberEditText(editTextCarrierNumber);

        //So that it can be used in the onClick method
        final CountryCodePicker forButton = ccp;

        //To check if we got the Phone number from Sim or not
        final boolean numFound;

        if(number.isEmpty()){
            numFound = false;
        }
        else{   //Will have to check this as this part of the code is never executed
            Toast.makeText(getApplicationContext(), "Your Phone Number is: "
                    +number, Toast.LENGTH_SHORT).show();
            ccp.setFullNumber(number);
            editTextCarrierNumber.setText(number);
            numFound = true;
            mTo = number;
        }

        //Initializing the Let's Go Button and setting onClick functionality
        checkNumButton = findViewById(R.id.checkNumButton);
        checkNumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CountryCode gives us something like PK;92;Pakistan so spliting the string to get only the code .i.e., 92
                String[] splitted= forButton.getSelectedCountryCode().split(":");

                //Adding + sign to the country code so that the phone number becomes valid
                forButton.setFullNumber("+" + splitted[0] + editTextCarrierNumber.getText().toString());

                //For Debugging purpose only
                Log.d ("Number: ", forButton.getFullNumberWithPlus());

                if(!numFound)
                    mTo = forButton.getFullNumberWithPlus();

                //Checking the validity of the phone number by checking it's Length
                if (forButton.isValidFullNumber()){
                    //Initializing the server to send the SMS
                    post(mContext.getString(R.string.host_name), new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        //If the msg is successfully recieved by the server .i.e., the 6 digit code which the server is supposed to send then onResponse method is called
                        @Override
                        public void onResponse(Call call, Response response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //This function is called when the SMS is successfully sent
                                    Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_SHORT).show();

                                    //Taking the user to the next screen to enter the verification code
                                    Intent i = new Intent(SignInScreen.this, PhoneVerificationScreen.class);
                                    i.putExtra("PHONE_NUMBER", mTo);
                                    i.putExtra("RANDOM_NUMBER", mBody);
                                    startActivity(i);
                                }
                            });
                        }
                    });

                }
                else{   //This will be executed if the given number by the user is not valid
                    Toast.makeText(getApplicationContext(), "Number not Valid! Please Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //This function is called when the SIM allows to share the number
    private String getMyPhoneNO() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService (Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        String yourNumber = mTelephonyMgr.getLine1Number();
        return yourNumber;
    }
}
