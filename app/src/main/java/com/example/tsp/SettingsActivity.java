package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;

    private CircleImageView UserProfileImage;
    private EditText Username,UserStatus;
    private String currentUserID;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private static final int galleryPick=1;
    private StorageReference userProfileImageRef;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        progressDialog=new ProgressDialog(SettingsActivity.this);

        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        initializeFields();
        retrieveUserInfo();
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        UserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);

//                Intent galleryIntent=new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent,galleryPick);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            progressDialog.setTitle("Setting Profile Picture");
            progressDialog.setMessage("Please wait, your profile picture is updating...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            if(resultCode==RESULT_OK){
                Uri resultUri=result.getUri();

                final StorageReference filepath=userProfileImageRef.child(currentUserID+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile picture uploaded successfully!",Toast.LENGTH_SHORT).show();
//                           final String downloadUrl=task.getResult().getStorage().getDownloadUrl().toString();

                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        String downloadUrl=task.getResult().toString();
                                        Log.d("tsp","imag  url  is: "+downloadUrl);

                                        RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            progressDialog.dismiss();
                                                            Toast.makeText(SettingsActivity.this,"Profile picture saved to Database successfully!",Toast.LENGTH_SHORT).show();

                                                        }else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(SettingsActivity.this,"Error: "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else{
                                        Toast.makeText(SettingsActivity.this,"Error: :"+task.getException().toString(),Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });



                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,"Error: "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        }
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

    private void retrieveUserInfo(){
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")){
                    String retrieveUsername=snapshot.child("name").getValue().toString();
                    String retrieveUserStatus=snapshot.child("status").getValue().toString();
                    String retrieveProfileImage=snapshot.child("image").getValue().toString();

                    Picasso.get().load(retrieveProfileImage).into(UserProfileImage);

                    Username.setText(retrieveUsername);
                    UserStatus.setText(retrieveUserStatus);

                }else if(snapshot.exists() && snapshot.hasChild("name") ){
                    String retrieveUsername=snapshot.child("name").getValue().toString();
                    String retrieveUserStatus=snapshot.child("status").getValue().toString();

                    Username.setText(retrieveUsername);
                    UserStatus.setText(retrieveUserStatus);

                }else{
                    Toast.makeText(SettingsActivity.this,"Please set and Update your Info",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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