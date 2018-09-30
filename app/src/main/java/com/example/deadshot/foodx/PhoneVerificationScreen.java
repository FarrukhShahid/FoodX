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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhoneVerificationScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification_screen);

        //For Getting the items sent from previous activity
        final Intent intent = getIntent();

        //This is the dynamic text which is shown below the heading
        SpannableString ss = new SpannableString("We have sent a message containing a 6-digit code to " + intent.getStringExtra("PHONE_NUMBER") + ". If this is not your Phone Number then Click Here.");
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

        //This will make the "Phone Number" BOLD as well as the text "CLICK HERE" a HYPERLINK

        int ClickFrom = 0;
        int PhoneFrom = 0;
        int PhoneTill = 0;
        for(int i = 0; i < ss.length() ;i++){   //Making it generic to find Click Here and the Number to make them separate from the text
            if(ss.charAt(i) == 'C' && ss.charAt(i+1) == 'l' && ss.charAt(i+2) == 'i' && ss.charAt(i+3) == 'c' && ss.charAt(i+4) == 'k')
                ClickFrom = i;
            if(ss.charAt(i) == '+' && ss.charAt(i+1) == '9' && ss.charAt(i+2) =='2')
                PhoneFrom = i;
            if(i < ss.length() - 3 && ss.charAt(i) == '.' && ss.charAt(i+1) == ' ' && ss.charAt(i+2) == 'I' && ss.charAt(i+3) == 'f')
                PhoneTill = i;
        }
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
        ss.setSpan(bss, PhoneFrom, PhoneTill, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(iss, ClickFrom, ClickFrom + 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(clickableSpan, ClickFrom, ClickFrom + 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), ClickFrom, ClickFrom + 10, 0);

        //Adding the lines to the Text View already present in the xml
        TextView textView = (TextView) findViewById(R.id.ConfirmationText);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

        final EditText ConfirmationNumber = (EditText)findViewById(R.id.ConfirmationNumber);

        Button LogIn = (Button) findViewById(R.id.GetStarted_Button);
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(),MainMapsActivity.class));
            if (!SignInScreen.check){
                return;
            }
            if (ConfirmationNumber.getText().toString().equals(intent.getStringExtra("RANDOM_NUMBER"))) {

                //A file will be created with the name of the Application Title with the Text LoginSuccess
                String FILE_NAME = getResources().getString(R.string.app_name);
                String txt = "LoginSuccess";
                FileOutputStream outputStream = null;
                File file = new File(FILE_NAME);

                //For Writing the File
                try {
                    outputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    outputStream.write(txt.getBytes());

                    //Can remove this toast
                    Toast.makeText(getApplicationContext(), "File Created", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    //Can remove this toast
                    Toast.makeText(getApplicationContext(), "File Not Created", Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        //Can remove this toast
                        Toast.makeText(getApplicationContext(), "File Not Closed", Toast.LENGTH_SHORT).show();
                    }
                }

                //This will change the screen if the user enters correct Random Number
                Intent myIntent = new Intent(getApplicationContext(), InitialProfileSetupScreen.class);
                startActivity(myIntent);
            }
            else {  //This part of the code will be executed when the user enters wrong code
                Toast.makeText(getApplicationContext(), "Number entered was wrong. Please Try Again!", Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
}
