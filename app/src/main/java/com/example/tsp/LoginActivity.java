package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    public FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private Button loginButton, phoneLoginButton;
    private TextView forget_password,need_new_account;
    private EditText userEmail, userPassword;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();


        mAuth = FirebaseAuth.getInstance();

        RootRef= FirebaseDatabase.getInstance().getReference();


        need_new_account.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });
    }

    private void allowUserToLogin() {

        String email = userEmail.getText().toString();
        final String password = userPassword.getText().toString();

        progressBar.setTitle("Logging in");
        progressBar.setMessage("Please wait...");
        progressBar.setCanceledOnTouchOutside(true);
        progressBar.show();

        if (TextUtils.isEmpty(email))
            Toast.makeText(this, "Please Enter an email...", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(password))
            Toast.makeText(this, "Please Enter a password...", Toast.LENGTH_SHORT).show();
        else {
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this,"Logged in successfully",Toast.LENGTH_SHORT).show();

                                String currentUserID=mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                                sendUserToMainActivity();
                            }
                            else{
                                try {
                                    throw task.getException();
                                }  catch(FirebaseAuthInvalidCredentialsException e) {
                                    userPassword.setError(password);
//                                    Toast.makeText(LoginActivity.this, "Email or Password incorrect!",
//                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {

                                }
                                Toast.makeText(LoginActivity.this, "Email or Password incorrect!",
                                        Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();

                            }
                        }

                    });
        }

    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void initializeFields() {
        loginButton=(Button)findViewById(R.id.login_button);
        phoneLoginButton=(Button)findViewById(R.id.login_button_phone);
        forget_password=(TextView)findViewById(R.id.forget_password);
        need_new_account=(TextView)findViewById(R.id.new_account_link);
        userEmail=(EditText)findViewById(R.id.login_email);
        userPassword=(EditText)findViewById(R.id.login_password);
        progressBar=new ProgressDialog(this);
    }


    @Override
    protected void onStart(){
        super.onStart();
        if(currentUser!=null){
           sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent MainActivityIntent=new Intent(LoginActivity.this,MainActivity.class);
        //prevent user from going back
        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainActivityIntent);
        finish();
    }
}