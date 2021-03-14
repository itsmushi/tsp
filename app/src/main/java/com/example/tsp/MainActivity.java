package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.view.Menu;

import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;


import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    ViewPager myviewPager;
    TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;
    public FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mToolbar= findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle("tsp");

        mAuth=FirebaseAuth.getInstance();

        currentUser=mAuth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();

        myviewPager=findViewById(R.id.main_tabs_pager);
        mytabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(mytabsAccessorAdapter);

        myTabLayout=findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myviewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter=new IntentFilter();

        filter.addAction(getPackageName()+"a");

        if(currentUser==null){
           sendUserToLoginActivity();

        }else{

           verifyUserExistence();
        }
   }

    private void verifyUserExistence() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        Log.d("tsp","here15");
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("tsp","here14");
                if((snapshot.child("name").exists() )){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }else{
                   sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginActivity=new Intent(this, LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivity);
        finish();
    }

    private void sendUserToSettingActivity() {
        Intent settingActivity=new Intent(this, SettingsActivity.class);
        settingActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingActivity);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("tsp","here13");
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("tsp","here11");
        if(item.getItemId()==R.id.logout_option){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.settings_option){
            sendUserToSettingActivity();
        }
        if(item.getItemId()==R.id.find_friends_option){

        }
        if(item.getItemId()==R.id.crate_group_option){
            requestNewGroup();
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);

        builder.setTitle("Enter Group Title:");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        groupNameField.setHint("e.g Sophomores..");
        builder.setView(groupNameField);
        Log.d("tsp","here25");
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this,"Enter Group name",Toast.LENGTH_SHORT).show();

                }else{
                    createGroupName(groupName);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert=builder.create();
        alert.show();

    }

    private void createGroupName(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName+"group created successfully",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}