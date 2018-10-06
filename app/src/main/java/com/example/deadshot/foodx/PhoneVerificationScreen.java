package com.example.deadshot.foodx;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhoneVerificationScreen extends AppCompatActivity {

    private TextView Number_TextView = null;
    private TextView textView = null;
    private AutoCompleteTextView ConfirmationText = null;

    private OkHttpClient mClient = null;
    private String PhoneNumber = null;
    private String VerificationCode = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification_screen);

        if(!SignInScreen.check)
            return;

        Random rand = new Random();
        VerificationCode = String.valueOf(rand.nextInt(999999) + 100000);
        mClient = new OkHttpClient();

        Number_TextView = findViewById(R.id.Verification_activity_PhoneNumber_TextView);
        ConfirmationText = findViewById(R.id.ConfirmationNumber);
        //For Getting the items sent from previous activity
        final Intent intent = getIntent();

        Bundle previous_data = getIntent().getExtras();
        PhoneNumber = previous_data.getString("PhoneNumber");
        Number_TextView.setText(PhoneNumber);

        final String sendingstr = getString(R.string.sending_code);
        final String linkstr = getString(R.string.edit_phone_number_link);
        final String sentstr = getString(R.string.sent_code);

        SpannableString ss = new SpannableString(sendingstr+linkstr);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                //If "Click here" is pressed then it will take the user to the activity given below
                startActivity(new Intent(getApplicationContext(), SignInScreen.class));
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
        ss.setSpan(iss, sendingstr.length(), sendingstr.length() + linkstr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(clickableSpan, sendingstr.length(), sendingstr.length() + linkstr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), sendingstr.length(), sendingstr.length() + linkstr.length(), 0);

        textView = (TextView) findViewById(R.id.ConfirmationText);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        post(getApplicationContext().getString(R.string.host_name), new Callback() {

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
                    Log.d("SMS->>>", VerificationCode.toString());
                    //Taking the user to the next screen to enter the verification code
                    SpannableString ss = new SpannableString(sentstr+linkstr);
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            //If "Click here" is pressed then it will take the user to the activity given below
                            startActivity(new Intent(getApplicationContext(), SignInScreen.class));
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                        }
                    };

                    final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
                    ss.setSpan(iss, sentstr.length(), sentstr.length() + linkstr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    ss.setSpan(clickableSpan, sentstr.length(), sentstr.length() + linkstr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new UnderlineSpan(), sentstr.length(), sentstr.length() + linkstr.length(), 0);

                    textView = (TextView) findViewById(R.id.ConfirmationText);
                    textView.setText(ss);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });
            }
        });
    }
    //This function is called to send the verification
    // SMS where "num" is the number on which the msg
    // is sent and "mBody" is the verification code
    Call post(String url, Callback callback) {
        String num = PhoneNumber.toString();
        RequestBody formBody = new FormBody.Builder().add("To", num).add("Body", VerificationCode).build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call response = mClient.newCall(request);
        response.enqueue(callback);
        return response;
    }
    public void Verify_code(View v){
        if (!SignInScreen.check){
            startActivity(new Intent(getApplicationContext(),MainMapsActivity.class));
            return;
        }
        Log.d("Verify Button->>",ConfirmationText.getText().toString()+":"+VerificationCode);
        if(ConfirmationText.getText().toString().equals(VerificationCode)){
            startActivity(new Intent(getApplicationContext(),MainMapsActivity.class));
        }
    }
}
