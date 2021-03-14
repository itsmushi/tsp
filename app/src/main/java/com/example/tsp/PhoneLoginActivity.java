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
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    Button sendVerificationCodeButton,verifyButton;
    EditText inputPhoneNumber, inputVerificationCode;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseAuth mAuth;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    ProgressDialog loadingBar;
    PhoneAuthCredential credential;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);

        sendVerificationCodeButton=(Button)findViewById(R.id.send_code_button);
        verifyButton=(Button)findViewById(R.id.verify_code_button);
        inputPhoneNumber=(EditText)findViewById(R.id.phone_number_input);
        inputVerificationCode=(EditText)findViewById(R.id.verification_code_input);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phoneNumber=inputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this,"Please  input phone number!",Toast.LENGTH_LONG).show();

                }
                else{
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait while we authenticate your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode=inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this,"Please write verification code first",Toast.LENGTH_LONG).show();
                }
                else {
                    loadingBar.setTitle("Code verification");
                    loadingBar.setMessage("Please wait while we verify your code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
//                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }


            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Please enter correct phone number with your country code! ",Toast.LENGTH_LONG).show();
                Log.d("tsp","error on code"+e);
                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerificationCodeButton.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Code has been sent!:",Toast.LENGTH_LONG).show();

                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };


    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            eLog.d(TAG, "signInWithCredential:success");
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Now you are logged in!",Toast.LENGTH_LONG).show();
                            sendUserToMainActivity();
//
//                            FirebaseUser user = task.getResult().getUser();
//                            // Update UI
                        } else {
                            loadingBar.dismiss();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(PhoneLoginActivity.this,"Sorry, you have entered invalid code!",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent MainActivityIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        //prevent user from going back
        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainActivityIntent);
        finish();
    }
}