package com.example.tsp;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String receiverUserId;
    CircleImageView userProfileImage;
    TextView userProfileName,userProfileStatus;
    Button sendMessageRequestButton;
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        receiverUserId=getIntent().getExtras().getString("visitUserId");

        userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserId);

        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_username);
        userProfileStatus=(TextView)findViewById(R.id.visit_status);
        sendMessageRequestButton=(Button)findViewById(R.id.send_request_message_button);

        retrieveUserInfo();
        
    }

    private void retrieveUserInfo() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {  //user has a profile picture set
                if(snapshot.exists() && snapshot.child("image").exists()){
                    String username=snapshot.child("name").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();
                    String image=snapshot.child("image").getValue().toString();

                    userProfileName.setText(username);
                    userProfileStatus.setText(status);
                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(userProfileImage);
                }
                else if(snapshot.exists()){
                    String username=snapshot.child("name").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();

                    userProfileName.setText(username);
                    userProfileStatus.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}