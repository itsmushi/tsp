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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeFields();

        mAuth=FirebaseAuth.getInstance();


        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        final String email=userEmail.getText().toString();
        final String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email))
            Toast.makeText(this,"Please Enter an email...", Toast.LENGTH_SHORT).show();
        if(TextUtils.isEmpty(password))
            Toast.makeText(this,"Please enter a password...",Toast.LENGTH_SHORT).show();
        else{
            progressBar.setTitle("Creating new Account");
            progressBar.setMessage("Please wait, while we are creating new account for you...");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Log.d("tsp","here2");
                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account created Successfully", Toast.LENGTH_LONG).show();
                            }

                            else
                            {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    userPassword.setError(password);
                                    Toast.makeText(RegisterActivity.this,"Password is too weak",Toast.LENGTH_LONG).show();
                                    userPassword.requestFocus();
                                }
                                catch(FirebaseAuthInvalidCredentialsException e) {
                                    userPassword.setError(password);
                                    Toast.makeText(RegisterActivity.this, "Please enter correct Email!",
                                            Toast.LENGTH_SHORT).show();
                                }catch(FirebaseAuthUserCollisionException e) {
                                    userEmail.setError(email);

                                    Toast.makeText(RegisterActivity.this,"User with that email already registered",Toast.LENGTH_LONG).show();
                                } catch(Exception e) {

                                }

                            }
                            progressBar.dismiss();
                        }

                    }
            );
        }
    }

    private void sendUserToMainActivity() {
        Intent MainActivityIntent=new Intent(RegisterActivity.this,MainActivity.class);
        //prevent user from going back
        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainActivityIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void initializeFields() {
        createAccountButton=(Button)findViewById(R.id.register_button);
        alreadyHaveAccount=(TextView)findViewById(R.id.new_account_link);
        userEmail=(EditText)findViewById(R.id.login_email);
        userPassword=(EditText)findViewById(R.id.login_password);
        progressBar=new ProgressDialog(this);

    }
}