package com.example.deadshot.foodx;


import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;


import com.hbb20.CountryCodePicker;


public class SignInScreen extends AppCompatActivity {

    private CountryCodePicker ccp;
    private Button LoginButton;
    private String mTo;


    public static boolean check = false;

    private EditText editTextCarrierNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);

        //Getting the number of the user if possible i.e., if SIM shares it with us

        Bundle previous_data = getIntent().getExtras();
        //Initializing the Country Picker
        ccp = findViewById(R.id.ccp);
        editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);

        //Setting the Edit Text to be associated with the country picker so that country picker can get the phone number as well as the country code
        ccp.registerCarrierNumberEditText(editTextCarrierNumber);

        //So that it can be used in the onClick method
        final CountryCodePicker forButton = ccp;
        //Initializing the Let's Go Button and setting onClick functionality
        LoginButton = findViewById(R.id.login_button);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!check) {
                    Intent i = new Intent(SignInScreen.this, PhoneVerificationScreen.class);
                    startActivity(i);
                    return;
                }
                String code = forButton.getSelectedCountryCode();
                Log.d("phone number ->>", forButton.getSelectedCountryCode().toString());
                mTo = "+" + code + editTextCarrierNumber.getText().toString();
                forButton.setFullNumber(mTo);
                Log.d("P_no with plus", forButton.getFullNumberWithPlus());
                if (forButton.isValidFullNumber()) {
                    Intent i = new Intent(getApplicationContext(), PhoneVerificationScreen.class);
                    i.putExtra("PhoneNumber", mTo);
                    startActivity(i);
                }
            }
        });
    }
}
