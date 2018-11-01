package com.example.deadshot.foodx;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class VerificationFragment extends Fragment {

    // Super tag
    private static final String TAG = "VerificationFragment";

    // UI Components
    private View _view = null;
    private TextView Number_TextView = null;
    private TextView textView = null;
    private EditText ConfirmationText = null;
    private EditText ConfirmationText1 = null;
    private EditText ConfirmationText2 = null;
    private EditText ConfirmationText3 = null;
    private EditText ConfirmationText4 = null;
    private EditText ConfirmationText5 = null;
    private AlertDialog.Builder builder;

    //Global Attributes * because of usage in different functions*
    private String PhoneNumber = "";
    private String sendingstr;
    private String linkstr;
    private String sentstr;

    //FireBase Variables
    private FirebaseAuth mAuth = null;
    private PhoneAuthCredential phoneAuthCredential = null;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = null;
    private String VerificationId = "verification id";
    private PhoneAuthProvider.ForceResendingToken Token = null;

    // Class to supply focus in different boxes for the verification code
    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            switch(view.getId()){
                case R.id.ConfirmationNumber:
                    if (!ConfirmationText.getText().toString().isEmpty())
                        ConfirmationText1.requestFocus();
                    break;
                case R.id.ConfirmationNumber1:
                    if (!ConfirmationText1.getText().toString().isEmpty())
                        ConfirmationText2.requestFocus();
                    break;
                case R.id.ConfirmationNumber2:
                    if (!ConfirmationText2.getText().toString().isEmpty())
                        ConfirmationText3.requestFocus();
                    break;
                case R.id.ConfirmationNumber3:
                    if (!ConfirmationText3.getText().toString().isEmpty())
                        ConfirmationText4.requestFocus();
                    break;
                case R.id.ConfirmationNumber4:
                    if (!ConfirmationText4.getText().toString().isEmpty())
                        ConfirmationText5.requestFocus();
                    break;
                case R.id.ConfirmationNumber5:
                    if (!ConfirmationText.getText().toString().isEmpty()) {
                        ConfirmationText5.clearFocus();

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_verification, container, false);

        //Initializing all the components
        initialize();

        // To pass through every functionality
        if(!SignInScreen.check){
            Log.d(TAG,"SignInScreen Check = true");
            return _view;
        }
        return _view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            Bundle previous_data = getActivity().getIntent().getExtras();
            PhoneNumber = LoginFragment.Phone_number;
            Number_TextView.setText(PhoneNumber);

            if (PhoneNumber == null){
                Log.d(TAG,"number = null");
                return;
            }
            if (PhoneNumber.isEmpty()){
                builder.setTitle("No Phone Number Provided")
                        .setMessage("You have to provide your phone number")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                SignInScreen.viewPager.setCurrentItem(0);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            update_instruction_string(sendingstr,linkstr);

            send_code_message(PhoneNumber,getActivity());


            //verify button click
            _view.findViewById(R.id.verifyButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String enteredCode = ConfirmationText.getText().toString();
                    enteredCode += ConfirmationText1.getText().toString();
                    enteredCode += ConfirmationText2.getText().toString();
                    enteredCode += ConfirmationText3.getText().toString();
                    enteredCode += ConfirmationText4.getText().toString();
                    enteredCode += ConfirmationText5.getText().toString();

                    check_code_authenticity_and_start_activity(
                            VerificationId,
                            enteredCode);
                }
            });
        }else{

        }
    }

    private void initialize(){
        Number_TextView = _view.findViewById(R.id.Verification_activity_PhoneNumber_TextView);
        ConfirmationText = _view.findViewById(R.id.ConfirmationNumber);
        ConfirmationText.addTextChangedListener(new GenericTextWatcher(ConfirmationText));
        ConfirmationText1 = _view.findViewById(R.id.ConfirmationNumber1);
        ConfirmationText1.addTextChangedListener(new GenericTextWatcher(ConfirmationText1));
        ConfirmationText2 = _view.findViewById(R.id.ConfirmationNumber2);
        ConfirmationText2.addTextChangedListener(new GenericTextWatcher(ConfirmationText2));
        ConfirmationText3 = _view.findViewById(R.id.ConfirmationNumber3);
        ConfirmationText3.addTextChangedListener(new GenericTextWatcher(ConfirmationText3));
        ConfirmationText4 = _view.findViewById(R.id.ConfirmationNumber4);
        ConfirmationText4.addTextChangedListener(new GenericTextWatcher(ConfirmationText4));
        ConfirmationText5 = _view.findViewById(R.id.ConfirmationNumber5);
        ConfirmationText5.addTextChangedListener(new GenericTextWatcher(ConfirmationText5));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        sendingstr = getString(R.string.sending_code);
        linkstr = getString(R.string.edit_phone_number_link);
        sentstr = getString(R.string.sent_code);

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                phoneAuthCredential = credential;
                Log.d(TAG, "onVerificationCompleted:");
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d(TAG, "onVerificationFailed", e);
            }

            @Override
            public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId,token);
                Log.d(TAG, "onCodeSent");
                update_instruction_string(sentstr,linkstr);
                VerificationId = verificationId;
                Token = token;
            }
        };

        Log.d(TAG,"Initialize");
    }

    public void send_code_message(String PhoneNumber, Activity activity){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                PhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        Log.d(TAG,"sending the code...");
    }

    public void check_code_authenticity_and_start_activity(
            String id,
            String code) {

        final PhoneAuthCredential c = PhoneAuthProvider.getCredential(id, code);
        mAuth.signInWithCredential(c).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = task.getResult().getUser();
                    startActivity(new Intent(getActivity().getApplicationContext(), MainMapsActivity.class));
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid

                    }
                }
            }
        });
    }
    public void update_instruction_string(String str,String link){
        SpannableString ss = new SpannableString(str+link);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                //If "Click here" is pressed then it will take the user to the activity given below
//                        startActivity(new Intent(getApplicationContext(), SignInScreen.class));
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
        ss.setSpan(iss, str.length(), str.length() + link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(clickableSpan, str.length(), str.length() + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), str.length(), str.length() + link.length(), 0);

        textView = (TextView) _view.findViewById(R.id.ConfirmationText);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
