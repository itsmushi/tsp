package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;

    private CircleImageView UserProfileImage;
    private EditText Username,UserStatus;
    private String currentUserID;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();


        initializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

    }

    private void UpdateSettings() {
        String setUsername=Username.getText().toString();
        String setStatus=UserStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername)){
            Toast.makeText(SettingsActivity.this,"Please enter a username!",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(SettingsActivity.this,"Please enter your status...",Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUsername);
            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,"Profile updated successfully!",Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                        }else{
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                            }
                    }

            });

        }
    }

    private void sendUserToMainActivity() {
        Intent MainActivityIntent=new Intent(SettingsActivity.this,MainActivity.class);
        //prevent user from going back
        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainActivityIntent);
        finish();
    }

    private void initializeFields() {
        UpdateAccountSettings=(Button)findViewById(R.id.update_buttton);
        Username=(EditText)findViewById(R.id.username);
        UserStatus=(EditText)findViewById(R.id.set_profile_status);
        UserProfileImage=(CircleImageView)findViewById(R.id.profile_image);
    }
}