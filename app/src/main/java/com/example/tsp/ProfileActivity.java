package com.example.tsp;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String receiverUserId,senderUserId,currentState;
    CircleImageView userProfileImage;
    TextView userProfileName,userProfileStatus;
    Button sendMessageRequestButton;
    DatabaseReference userRef,chatRequestRef;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        receiverUserId=getIntent().getExtras().getString("visitUserId");

        userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserId);
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_username);
        userProfileStatus=(TextView)findViewById(R.id.visit_status);
        sendMessageRequestButton=(Button)findViewById(R.id.send_request_message_button);

        currentState="new";
        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

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

                    manageChatRequests();

                }
                else if(snapshot.exists()){
                    String username=snapshot.child("name").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();

                    userProfileName.setText(username);
                    userProfileStatus.setText(status);
                    manageChatRequests();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        currentState="request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!senderUserId.equals(receiverUserId)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                }
            });
        }else{
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void cancelChatRequest() {
        chatRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState="new";
                                                sendMessageRequestButton.setText("Send Message");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiverUserId).child(senderUserId)
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                currentState="request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                        }
                    });
                }
            }
        });

    }
}