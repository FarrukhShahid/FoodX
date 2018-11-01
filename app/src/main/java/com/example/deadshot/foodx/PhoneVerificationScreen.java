package com.example.deadshot.foodx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private EditText ConfirmationText = null;
    private EditText ConfirmationText1 = null;
    private EditText ConfirmationText2 = null;
    private EditText ConfirmationText3 = null;
    private EditText ConfirmationText4 = null;
    private EditText ConfirmationText5 = null;
    private static final String TAG = "PhoneAuthActivity";

    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification_screen);

        Log.d("Check","--->>> initialized");


        //For Getting the items sent from previous activity

        final Intent intent = getIntent();

        Log.d(TAG,"onCreate Completed");
    }

    public void Verify_code(View v){
        if (!SignInScreen.check){
            startActivity(new Intent(getApplicationContext(),MainMapsActivity.class));
            return;
        }


//        if(ConfirmationText.getText().toString().equals(VerificationCode)){
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference databaseReference = database.getReference();
//            databaseReference.child("root").push().setValue(PhoneNumber);
//            startActivity(new Intent(getApplicationContext(),MainMapsActivity.class));
//        }
    }
}
