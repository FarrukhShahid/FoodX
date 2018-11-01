package com.example.deadshot.foodx;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

public class LoginFragment extends Fragment {


    private CountryCodePicker ccp;
    private Button LoginButton;
    private String mTo;
    private EditText editTextCarrierNumber = null;
    private View view;
    private AlertDialog.Builder builder;

    private final String Tag = "LoginFragment";
    public static String Phone_number = "number";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        //Setting the Edit Text to be associated with the country picker so that country picker
        // can get the phone number as well as the country code
        Bundle previous_data = getActivity().getIntent().getExtras();

        //Initializing the ui components
        initialize();

        ccp.registerCarrierNumberEditText(editTextCarrierNumber);
        String number = previous_data.getString("PhoneNumber");
        if (number != null){
            editTextCarrierNumber.setText(number);
            Log.d(Tag,"number = null");
        }
        //So that it can be used in the onClick method
        final CountryCodePicker forButton = ccp;
        //Initializing the Let's Go Button and setting onClick functionality

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (!SignInScreen.check) {
                SignInScreen.viewPager.setCurrentItem(1);
                return;
            }
            String code = forButton.getSelectedCountryCode();
            Log.d("phone number ->>", forButton.getSelectedCountryCode().toString());
            mTo = "+" + code + editTextCarrierNumber.getText().toString();
            forButton.setFullNumber(mTo);
            Log.d("P_no with plus", forButton.getFullNumberWithPlus());
            if (forButton.isValidFullNumber()) {
                Phone_number = mTo;
                SignInScreen.viewPager.setCurrentItem(1);
//                viewPager.setCurrentItem(1,true);
            }else{
                builder.setTitle("Invalid Phone Number")
                        .setMessage("You have to provide authentic phone number").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setIcon(android.R.drawable.ic_dialog_info).show();
            }
            }
        });
        return view;
    }
    private void initialize(){
        ccp = view.findViewById(R.id.ccp);
        editTextCarrierNumber = view.findViewById(R.id.editText_carrierNumber);
        LoginButton = view.findViewById(R.id.login_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
    }
}
