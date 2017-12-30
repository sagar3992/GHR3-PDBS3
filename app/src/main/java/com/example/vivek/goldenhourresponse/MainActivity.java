package com.example.vivek.goldenhourresponse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    //Declare inside class
    EditText etName, etAge, etPhone;
    //RadioGroup is used and no need for RadioButtons to be declared
    RadioGroup rgGender;
    Button btnRegister;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind inside onCreate()
        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        etPhone = (EditText) findViewById(R.id.etPhone);
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        //Create a file for SharedPreferences
        sp = getSharedPreferences("User", MODE_PRIVATE);

        //If name is entered, directly go to help activity
        if (sp.getString("n", "") != "") {
            String name = sp.getString("n", "");
            Intent i = new Intent(MainActivity.this, FirstActivity.class);
            //Store name as key-value pair using putExtra() method
            i.putExtra("n", name);
            startActivity(i);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                char[] ch = name.toCharArray();
                String age = etAge.getText().toString();
                String phone = etPhone.getText().toString();
                if (name.length() == 0) {
                    etName.setError("Please enter your name");
                    etName.requestFocus();
                    return;
                }
                //Check if only character is entered in name
                for (int i = 0; i < ch.length; i++) {
                    if (((ch[i] >= 65) && (ch[i] <= 90)) || ((ch[i] >= 97) && (ch[i] <= 122)) || (ch[i] == ' ')) {
                    } else {
                        etName.setError("Please enter a valid name");
                        etName.requestFocus();
                        return;
                    }
                }
                if (age.length() == 0) {
                    etAge.setError("Please enter your age");
                    etAge.requestFocus();
                    return;
                }
                if (phone.length() == 0) {
                    etPhone.setError("Please enter your phone number");
                    etPhone.requestFocus();
                    return;
                }
                if (phone.length() != 10) {
                    etPhone.setError("Please enter a valid phone number");
                    etPhone.requestFocus();
                    return;
                }

                /*//Group ---> Button ---> Text
                rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        RadioButton rb = (RadioButton) radioGroup.findViewById(i);  //Group ---> Button
                        String gender = rb.getText().toString();    //Button ---> Text
                    }
                });*/

                //Group-->id-->Button-->text
                int id = rgGender.getCheckedRadioButtonId();
                RadioButton rbGender = (RadioButton) findViewById(id);
                String gender = rbGender.getText().toString();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("n", name);
                editor.putInt("a", Integer.parseInt(age));
                //phone is stored as a String only
                editor.putString("p", phone);
                editor.putString("g", gender);
                //To save data persistently
                editor.commit();
                Intent i = new Intent(MainActivity.this, FirstActivity.class);
                //Send name alongwith Intent
                i.putExtra("n", name);
                startActivity(i);
                //After registering user, he/she should not come back to register again
                finish();
            }
        });

        //To check if data is stored in SharedPreferences
        /*
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = sp.getString("n", "");
                int age = sp.getInt("a", 0);
                String phone = sp.getString("p", "");
                etName.setText(name);
                //To convert int into String, we use String.valueOf(int)
                etAge.setText(String.valueOf(age));
                etPhone.setText(phone);
            }
        });*/
    }
}